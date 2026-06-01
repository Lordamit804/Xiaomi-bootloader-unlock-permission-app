package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DeviceTimer::class, ChecklistStep::class], version = 1, exportSchema = false)
abstract class UnlockDatabase : RoomDatabase() {
    abstract fun unlockDao(): UnlockDao

    companion object {
        @Volatile
        private var INSTANCE: UnlockDatabase? = null

        fun getDatabase(context: Context): UnlockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UnlockDatabase::class.java,
                    "unlock_permission_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
