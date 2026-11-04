package com.example.eventpass.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para almacenar los PINs escaneados cuando el teléfono NO tiene conexión a internet.
 * Su estructura coincide con el contrato POST /api/checkin/sync del backend en Render.
 */
@Entity(tableName = "pending_checkins")
data class PendingCheckInEntity(
    @PrimaryKey
    val idLocal: String = UUID.randomUUID().toString(),
    val eventoId: String,
    val qrCode: String,
    val timestampLocal: String
)

