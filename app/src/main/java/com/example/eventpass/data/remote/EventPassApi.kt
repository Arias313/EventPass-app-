package com.example.eventpass.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Modelo de solicitud para autenticación en la API Node.js/Express.
 * Incluye compatibilidad para endpoints que esperen userOrEmail, email o username.
 */
data class LoginRequest(
    @SerializedName("userOrEmail")
    val userOrEmail: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("email")
    val email: String = userOrEmail,

    @SerializedName("username")
    val username: String = userOrEmail
)

/**
 * Modelo de respuesta con el token JWT devuelto por el backend.
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("accessToken")
    val accessToken: String? = null,

    @SerializedName("message")
    val message: String? = null
) {
    /**
     * Retorna el token prioritario garantizando compatibilidad con token o accessToken.
     */
    val jwtToken: String
        get() = token ?: accessToken ?: ""
}

/**
 * DTO de Evento alineado con el normalizador de la plataforma web (MongoDB Atlas).
 */
data class EventoDto(
    @SerializedName("_id")
    val mongoId: String? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("titulo")
    val titulo: String? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("fecha")
    val fecha: String? = null,

    @SerializedName("lugar")
    val lugar: String? = null,

    @SerializedName("aforoTotal")
    val aforoTotal: Int? = null,

    @SerializedName("aforo")
    val aforo: Int? = null,

    @SerializedName("registrados")
    val registrados: Int? = null,

    @SerializedName("confirmados")
    val confirmados: Int? = null
) {
    val eventoId: String
        get() = mongoId ?: id ?: ""

    val displayName: String
        get() = titulo ?: nombre ?: "Evento sin título"

    val capacity: Int
        get() = aforoTotal ?: aforo ?: 0

    val confirmedCount: Int
        get() = confirmados ?: 0
}

data class EventosResponse(
    @SerializedName("eventos")
    val eventos: List<EventoDto> = emptyList()
)

/**
 * DTO de Asistente alineado con el backend en Render.
 */
data class AsistenteDto(
    @SerializedName("_id")
    val mongoId: String? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("eventoId")
    val eventoId: String? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("cedula")
    val cedula: String? = null,

    @SerializedName("correo")
    val correo: String? = null,

    @SerializedName("pin")
    val pin: String? = null,

    @SerializedName("estadoCheckin")
    val estadoCheckin: Boolean? = null,

    @SerializedName("checkin")
    val checkin: Boolean? = null,

    @SerializedName("fechaCheckin")
    val fechaCheckin: String? = null,

    @SerializedName("fechaRegistro")
    val fechaRegistro: String? = null
) {
    val asistenteId: String
        get() = mongoId ?: id ?: pin ?: ""

    val isCheckedIn: Boolean
        get() = estadoCheckin ?: checkin ?: false
}

data class AsistentesResponse(
    @SerializedName("asistentes")
    val asistentes: List<AsistenteDto> = emptyList()
)

/**
 * Modelo de solicitud para enviar el código QR o PIN escaneado.
 */
data class CheckInRequest(
    @SerializedName("qrCode")
    val qrCode: String
)

/**
 * Modelo de respuesta del servidor al validar el QR en POST /api/eventos/{eventoId}/checkin.
 */
data class CheckInResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("asistente")
    val asistente: AsistenteDto? = null
)

/**
 * Modelos para sincronización por lotes (Offline Batch Sync: POST /api/checkin/sync).
 */
data class BatchCheckInItemDto(
    @SerializedName("idLocal")
    val idLocal: String,

    @SerializedName("eventoId")
    val eventoId: String,

    @SerializedName("qrCode")
    val qrCode: String,

    @SerializedName("timestampLocal")
    val timestampLocal: String
)

data class BatchSyncRequest(
    @SerializedName("checkins")
    val checkins: List<BatchCheckInItemDto>
)

data class BatchSyncResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null
)

/**
 * Interfaz Retrofit para el servicio REST de EventPass en Render.
 */
interface EventPassApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/eventos")
    suspend fun getEventos(): Response<EventosResponse>

    @GET("api/eventos/{eventoId}/asistentes")
    suspend fun getAsistentes(
        @Path("eventoId") eventoId: String
    ): Response<AsistentesResponse>

    @POST("api/eventos/{eventoId}/checkin")
    suspend fun validateCheckIn(
        @Path("eventoId") eventoId: String,
        @Body request: CheckInRequest
    ): Response<CheckInResponse>

    @POST("api/checkin/sync")
    suspend fun syncPendingCheckIns(
        @Body request: BatchSyncRequest
    ): Response<BatchSyncResponse>
}
