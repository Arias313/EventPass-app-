package com.example.eventpass.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eventpass.data.remote.AsistenteDto
import com.example.eventpass.domain.model.Assistant

/**
 * Entidad Room para almacenar localmente la copia de asistentes enviada por MongoDB Atlas.
 * Indexada por eventoId y pin para búsquedas instantáneas durante el escaneo Offline.
 */
@Entity(
    tableName = "attendees",
    indices = [
        Index(value = ["eventoId"]),
        Index(value = ["pin"])
    ]
)
data class AttendeeEntity(
    @PrimaryKey
    val id: String,
    val eventoId: String,
    val nombre: String,
    val cedula: String,
    val correo: String,
    val pin: String,
    val estadoCheckin: Boolean,
    val fechaCheckin: String? = null
) {
    fun toDomainModel(): Assistant {
        val timeDisplay = if (estadoCheckin) {
            fechaCheckin?.let { iso ->
                val tIndex = iso.indexOf('T')
                if (tIndex != -1 && iso.length >= tIndex + 6) {
                    iso.substring(tIndex + 1, tIndex + 6)
                } else {
                    "Confirmado"
                }
            } ?: "Confirmado"
        } else {
            "Registrado"
        }

        return Assistant(
            id = id,
            name = nombre.ifBlank { "Asistente" },
            folio = pin.ifBlank { "Sin PIN" },
            timestamp = timeDisplay
        )
    }

    companion object {
        fun fromDto(dto: AsistenteDto, fallbackEventoId: String): AttendeeEntity {
            val resolvedPin = dto.pin?.trim()?.uppercase() ?: ""
            val resolvedId = dto.asistenteId.ifBlank { "${fallbackEventoId}_$resolvedPin" }
            return AttendeeEntity(
                id = resolvedId,
                eventoId = dto.eventoId?.takeIf { it.isNotBlank() } ?: fallbackEventoId,
                nombre = dto.nombre?.trim() ?: "Asistente",
                cedula = dto.cedula?.trim() ?: "",
                correo = dto.correo?.trim() ?: "",
                pin = resolvedPin,
                estadoCheckin = dto.isCheckedIn,
                fechaCheckin = dto.fechaCheckin
            )
        }
    }
}

