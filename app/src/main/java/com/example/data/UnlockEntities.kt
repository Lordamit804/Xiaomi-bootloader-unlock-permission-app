package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_timers")
data class DeviceTimer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceName: String,
    val miAccount: String,
    val bindTimeMillis: Long,
    val durationHours: Int = 168, // Default 168 hours (7 days)
    val notes: String = "",
    val isCompleted: Boolean = false
) {
    fun getEndTimeMillis(): Long {
        return bindTimeMillis + (durationHours * 60 * 60 * 1000L)
    }

    fun getTimeRemainingMillis(): Long {
        val remaining = getEndTimeMillis() - System.currentTimeMillis()
        return if (remaining < 0) 0 else remaining
    }
}

@Entity(tableName = "checklist_steps")
data class ChecklistStep(
    @PrimaryKey val stepKey: String,
    val isCompleted: Boolean
)
