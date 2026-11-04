package com.example.eventpass.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos SQLite local (Room) para soporte Offline-First en EventPass.
 */
@Database(
    entities = [
        AttendeeEntity::class,
        PendingCheckInEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EventPassDatabase : RoomDatabase() {

    abstract fun attendeeDao(): AttendeeDao

    companion object {
        private const val DATABASE_NAME = "eventpass_offline.db"

        @Volatile
        private var INSTANCE: EventPassDatabase? = null

        fun getInstance(context: Context): EventPassDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventPassDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

