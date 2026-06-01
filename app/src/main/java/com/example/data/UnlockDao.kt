package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    // Timers Queries
    @Query("SELECT * FROM device_timers ORDER BY bindTimeMillis DESC")
    fun getAllTimersFlow(): Flow<List<DeviceTimer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: DeviceTimer)

    @Delete
    suspend fun deleteTimer(timer: DeviceTimer)

    @Query("DELETE FROM device_timers WHERE id = :id")
    suspend fun deleteTimerById(id: Int)

    // Checklist Queries
    @Query("SELECT * FROM checklist_steps")
    fun getAllChecklistStepsFlow(): Flow<List<ChecklistStep>>

    @Query("SELECT * FROM checklist_steps WHERE stepKey = :stepKey")
    suspend fun getChecklistStep(stepKey: String): ChecklistStep?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistStep(step: ChecklistStep)
}
