package com.example.eventpass.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.remote.CheckInRequest
import com.example.eventpass.data.remote.RetrofitClient
import com.example.eventpass.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados de UI para la pantalla del escáner QR.
 */
sealed interface ScannerState {
    data object Idle : ScannerState // Listo para leer
    data object Processing : ScannerState // Procesando lectura con backend en Render
    data class Valid(val message: String, val code: String) : ScannerState // Acceso autorizado
    data class Invalid(val message: String, val code: String) : ScannerState // Acceso denegado o error
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val apiService = RetrofitClient.apiService

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    /**
     * Obtiene el ID del evento activo desde SessionManager o consulta el primer evento del usuario en Render.
     */
    private suspend fun resolveActiveEventoId(): String? {
        val savedId = sessionManager.getSelectedEventId()
        if (!savedId.isNullOrBlank()) return savedId

        return runCatching {
            val res = apiService.getEventos()
            val firstEvent = res.body()?.eventos?.firstOrNull()
            if (firstEvent != null && firstEvent.eventoId.isNotBlank()) {
                sessionManager.saveSelectedEvent(firstEvent.eventoId, firstEvent.displayName)
                firstEvent.eventoId
            } else {
                null
            }
        }.getOrNull()
    }

    /**
     * Procesa el código QR escaneado contra POST /api/eventos/{eventoId}/checkin en Render.
     */
    fun onQrScanned(qrCode: String) {
        if (_scannerState.value !is ScannerState.Idle) return

        val cleanCode = qrCode.trim()
        if (cleanCode.isEmpty()) return

        viewModelScope.launch {
            _scannerState.value = ScannerState.Processing

            try {
                val eventoId = resolveActiveEventoId()
                if (eventoId.isNullOrBlank()) {
                    _scannerState.value = ScannerState.Invalid(
                        message = "No tienes eventos creados en la plataforma web",
                        code = cleanCode
                    )
                    delay(2500)
                    _scannerState.value = ScannerState.Idle
                    return@launch
                }

                // 1. Llamada real a la API en Render
                val request = CheckInRequest(qrCode = cleanCode)
                val response = apiService.validateCheckIn(eventoId, request)

                // 2. Evaluación estructural de la respuesta (ok | ya_registrado | 404)
                if (response.isSuccessful) {
                    val body = response.body()
                    val attendeeName = body?.asistente?.nombre?.takeIf { it.isNotBlank() }
                    val displayPin = body?.asistente?.pin?.takeIf { it.isNotBlank() } ?: cleanCode

                    when (body?.status) {
                        "ok" -> {
                            val msg = if (attendeeName != null) {
                                "¡Acceso Autorizado!\n$attendeeName"
                            } else {
                                body.message ?: "¡Acceso Autorizado!"
                            }
                            _scannerState.value = ScannerState.Valid(
                                message = msg,
                                code = displayPin
                            )
                        }

                        "ya_registrado" -> {
                            val msg = if (attendeeName != null) {
                                "Pase ya utilizado anteriormente\n($attendeeName)"
                            } else {
                                "El boleto ya fue usado"
                            }
                            _scannerState.value = ScannerState.Invalid(
                                message = msg,
                                code = displayPin
                            )
                        }

                        else -> {
                            _scannerState.value = ScannerState.Invalid(
                                message = body?.message ?: body?.error ?: "Estado desconocido",
                                code = displayPin
                            )
                        }
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Boleto no encontrado en este evento"
                        401 -> "Sesión expirada. Vuelve a iniciar sesión"
                        else -> "Error del servidor (${response.code()})"
                    }
                    _scannerState.value = ScannerState.Invalid(
                        message = errorMsg,
                        code = cleanCode
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _scannerState.value = ScannerState.Invalid(
                    message = "Fallo de conexión con el servidor",
                    code = cleanCode
                )
            }

            // Muestra el resultado estructural durante 2.5 segundos
            delay(2500)

            // Restaura el escáner a "listo para leer"
            _scannerState.value = ScannerState.Idle
        }
    }

    /**
     * Alias de compatibilidad para procesar el boleto.
     */
    fun validateTicket(qrCode: String) = onQrScanned(qrCode)

    /**
     * Restablece el escáner a Idle de forma manual.
     */
    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}