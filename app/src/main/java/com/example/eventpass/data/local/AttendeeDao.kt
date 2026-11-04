package com.example.eventpass.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de Room para gestionar la caché local de asistentes y la cola de check-ins pendientes (Offline-First).
 */
@Dao
interface AttendeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendees: List<AttendeeEntity>)

    @Query("SELECT * FROM attendees WHERE eventoId = :eventoId ORDER BY estadoCheckin DESC, nombre ASC")
    suspend fun getAllByEvent(eventoId: String): List<AttendeeEntity>

    @Query("SELECT * FROM attendees ORDER BY estadoCheckin DESC, nombre ASC")
    suspend fun getAll(): List<AttendeeEntity>

    @Query("SELECT * FROM attendees WHERE eventoId = :eventoId AND UPPER(pin) = UPPER(:pin) LIMIT 1")
    suspend fun findByPin(eventoId: String, pin: String): AttendeeEntity?

    @Query(
        """
        UPDATE attendees 
        SET estadoCheckin = :estadoCheckin, fechaCheckin = :fechaCheckin 
        WHERE eventoId = :eventoId AND UPPER(pin) = UPPER(:pin)
        """
    )
    suspend fun updateStatus(
        eventoId: String,
        pin: String,
        estadoCheckin: Boolean,
        fechaCheckin: String?
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingCheckIn(pendingCheckIn: PendingCheckInEntity)

    @Query("SELECT * FROM pending_checkins ORDER BY timestampLocal ASC")
    suspend fun getPendingCheckIns(): List<PendingCheckInEntity>

    @Query("DELETE FROM pending_checkins WHERE idLocal IN (:ids)")
    suspend fun deletePendingCheckIns(ids: List<String>)
}

