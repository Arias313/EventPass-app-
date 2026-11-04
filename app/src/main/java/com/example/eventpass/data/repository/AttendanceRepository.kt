package com.example.eventpass.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.eventpass.data.local.AttendeeDao
import com.example.eventpass.data.local.AttendeeEntity
import com.example.eventpass.data.local.EventPassDatabase
import com.example.eventpass.data.local.PendingCheckInEntity
import com.example.eventpass.data.remote.BatchCheckInItemDto
import com.example.eventpass.data.remote.BatchSyncRequest
import com.example.eventpass.data.remote.CheckInRequest
import com.example.eventpass.data.remote.EventPassApi
import com.example.eventpass.data.remote.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Resultado unificado de validación de boleto (Online en Render u Offline en Room).
 */
sealed interface CheckInOutcome {
    data class Valid(
        val message: String,
        val code: String,
        val isOffline: Boolean = false
    ) : CheckInOutcome

    data class Invalid(
        val message: String,
        val code: String
    ) : CheckInOutcome
}

/**
 * Repositorio Offline-First que unifica las llamadas a la API REST en Render
 * y la persistencia local en SQLite mediante Room.
 */
class AttendanceRepository(
    context: Context,
    private val apiService: EventPassApi = RetrofitClient.apiService,
    private val attendeeDao: AttendeeDao = EventPassDatabase.getInstance(context).attendeeDao()
) {

    private val appContext = context.applicationContext

    /**
     * Verifica si el dispositivo cuenta con una conexión activa a internet.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Obtiene la lista de asistentes del evento:
     * - Si hay red: sincroniza pendientes, descarga de Render, guarda copia local en Room y retorna.
     * - Si no hay red o falla el servidor: retorna la copia local almacenada en Room.
     */
    suspend fun getAttendeesForEvent(eventoId: String): List<AttendeeEntity> {
        if (isNetworkAvailable()) {
            syncPendingCheckIns()
            try {
                val response = apiService.getAsistentes(eventoId)
                if (response.isSuccessful) {
                    val remoteList = response.body()?.asistentes.orEmpty()
                    val entities = remoteList.map { dto ->
                        AttendeeEntity.fromDto(dto, fallbackEventoId = eventoId)
                    }
                    attendeeDao.insertAll(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return attendeeDao.getAllByEvent(eventoId)
    }

    /**
     * Valida el código QR escaneado:
     * - Con internet: envía POST /api/eventos/{eventoId}/checkin a Render y actualiza Room.
     * - Sin internet (o ante caída de red): valida contra la tabla local `attendees` en Room
     *   y encola el registro en `pending_checkins` para sincronización posterior.
     */
    suspend fun validateAndRegisterCheckIn(
        eventoId: String,
        rawQrCode: String
    ): CheckInOutcome {
        val cleanPayload = rawQrCode.trim()

        // Seguridad Informática (Pilar 2.2): Sanitización contra QR maliciosos, URLs o inyecciones NoSQL/XSS
        if (!isSafeQrPayload(cleanPayload)) {
            return CheckInOutcome.Invalid(
                message = "Formato de QR bloqueado por seguridad",
                code = cleanPayload.take(24)
            )
        }

        val extractedPin = extractPinFromPayload(cleanPayload)

        if (isNetworkAvailable()) {
            try {
                val response = apiService.validateCheckIn(
                    eventoId = eventoId,
                    request = CheckInRequest(qrCode = cleanPayload)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val attendeeName = body?.asistente?.nombre?.takeIf { it.isNotBlank() }
                    val displayPin = body?.asistente?.pin?.takeIf { it.isNotBlank() } ?: extractedPin
                    val nowIso = currentIsoTimestamp()

                    return when (body?.status) {
                        "ok" -> {
                            attendeeDao.updateStatus(
                                eventoId = eventoId,
                                pin = displayPin,
                                estadoCheckin = true,
                                fechaCheckin = body.asistente?.fechaCheckin ?: nowIso
                            )
                            val msg = if (attendeeName != null) {
                                "¡Acceso Autorizado!\n$attendeeName"
                            } else {
                                body.message ?: "¡Acceso Autorizado!"
                            }
                            CheckInOutcome.Valid(message = msg, code = displayPin, isOffline = false)
                        }

                        "ya_registrado" -> {
                            attendeeDao.updateStatus(
                                eventoId = eventoId,
                                pin = displayPin,
                                estadoCheckin = true,
                                fechaCheckin = body.asistente?.fechaCheckin ?: nowIso
                            )
                            val msg = if (attendeeName != null) {
                                "Pase ya utilizado anteriormente\n($attendeeName)"
                            } else {
                                "El boleto ya fue usado"
                            }
                            CheckInOutcome.Invalid(message = msg, code = displayPin)
                        }

                        else -> CheckInOutcome.Invalid(
                            message = body?.message ?: body?.error ?: "Estado desconocido",
                            code = displayPin
                        )
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Boleto no encontrado en este evento"
                        401 -> "Sesión expirada. Vuelve a iniciar sesión"
                        else -> "Error del servidor (${response.code()})"
                    }
                    return CheckInOutcome.Invalid(message = errorMsg, code = extractedPin)
                }
            } catch (e: Exception) {
                // Si la petición HTTP falla por corte de red, conmutamos automáticamente a validación Offline en Room
                e.printStackTrace()
                return validateOfflineWithRoom(eventoId, cleanPayload, extractedPin)
            }
        } else {
            return validateOfflineWithRoom(eventoId, cleanPayload, extractedPin)
        }
    }

    /**
     * Validación local en SQLite (Room) cuando el teléfono no tiene conexión a internet.
     */
    private suspend fun validateOfflineWithRoom(
        eventoId: String,
        rawPayload: String,
        extractedPin: String
    ): CheckInOutcome {
        val nowIso = currentIsoTimestamp()
        val localAttendee = attendeeDao.findByPin(eventoId, extractedPin)

        if (localAttendee != null) {
            return if (!localAttendee.estadoCheckin) {
                // 1. Marcar como ingresado en la copia local de Room
                attendeeDao.updateStatus(
                    eventoId = eventoId,
                    pin = localAttendee.pin,
                    estadoCheckin = true,
                    fechaCheckin = nowIso
                )
                // 2. Guardar en la cola de pendientes para sincronizar cuando vuelva internet
                attendeeDao.insertPendingCheckIn(
                    PendingCheckInEntity(
                        eventoId = eventoId,
                        qrCode = localAttendee.pin,
                        timestampLocal = nowIso
                    )
                )
                CheckInOutcome.Valid(
                    message = "¡Acceso Autorizado (Offline)!\n${localAttendee.nombre}",
                    code = localAttendee.pin,
                    isOffline = true
                )
            } else {
                CheckInOutcome.Invalid(
                    message = "Pase ya utilizado (Offline)\n(${localAttendee.nombre})",
                    code = localAttendee.pin
                )
            }
        }

        // Si no se encontró en Room, verificamos si ya existía caché descargada para este evento
        val cachedEventAttendees = attendeeDao.getAllByEvent(eventoId)
        return if (cachedEventAttendees.isNotEmpty()) {
            CheckInOutcome.Invalid(
                message = "Boleto no encontrado en padrón local (Offline)",
                code = extractedPin
            )
        } else {
            // Si aún no había padrón local, encolamos el check-in para validación al recuperar red
            attendeeDao.insertPendingCheckIn(
                PendingCheckInEntity(
                    eventoId = eventoId,
                    qrCode = rawPayload,
                    timestampLocal = nowIso
                )
            )
            CheckInOutcome.Valid(
                message = "Guardado en cola Offline para sincronizar",
                code = extractedPin,
                isOffline = true
            )
        }
    }

    /**
     * Sincroniza con Render (POST /api/checkin/sync) todos los PINs escaneados sin conexión.
     */
    suspend fun syncPendingCheckIns(): Int {
        if (!isNetworkAvailable()) return 0
        val pendingList = attendeeDao.getPendingCheckIns()
        if (pendingList.isEmpty()) return 0

        return try {
            val batchItems = pendingList.map { entity ->
                BatchCheckInItemDto(
                    idLocal = entity.idLocal,
                    eventoId = entity.eventoId,
                    qrCode = entity.qrCode,
                    timestampLocal = entity.timestampLocal
                )
            }
            val response = apiService.syncPendingCheckIns(BatchSyncRequest(checkins = batchItems))
            if (response.isSuccessful) {
                attendeeDao.deletePendingCheckIns(pendingList.map { it.idLocal })
                pendingList.size
            } else {
                // Respaldo individual si el endpoint por lotes no procesa algún ítem
                val syncedIds = mutableListOf<String>()
                for (item in pendingList) {
                    val singleRes = runCatching {
                        apiService.validateCheckIn(
                            eventoId = item.eventoId,
                            request = CheckInRequest(qrCode = item.qrCode)
                        )
                    }.getOrNull()
                    if (singleRes != null && (singleRes.isSuccessful || singleRes.code() == 404)) {
                        syncedIds.add(item.idLocal)
                    }
                }
                if (syncedIds.isNotEmpty()) {
                    attendeeDao.deletePendingCheckIns(syncedIds)
                }
                syncedIds.size
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Valida que el payload del código QR cumpla con la estructura de EventPass
     * y no contenga scripts, URLs externas ni caracteres de inyección NoSQL/SQL.
     */
    private fun isSafeQrPayload(payload: String): Boolean {
        if (payload.length !in 4..120) return false
        val lower = payload.lowercase(Locale.ROOT)
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("javascript:")) {
            return false
        }
        val forbiddenChars = charArrayOf('$', '{', '}', '<', '>', ';', '\'', '"', '\\')
        return payload.none { it in forbiddenChars }
    }

    /**
     * Extrae el PIN en caso de que el QR venga en formato compuesto "EP|Nombre|EP-XXXX".
     */
    private fun extractPinFromPayload(payload: String): String {
        return if (payload.contains("|")) {
            payload.split("|").last().trim().uppercase()
        } else {
            payload.trim().uppercase()
        }
    }

    private fun currentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
    }
}

