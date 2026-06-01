package com.example.data

import kotlinx.coroutines.flow.Flow

class UnlockRepository(private val unlockDao: UnlockDao) {
    val allTimers: Flow<List<DeviceTimer>> = unlockDao.getAllTimersFlow()
    val allChecklistSteps: Flow<List<ChecklistStep>> = unlockDao.getAllChecklistStepsFlow()

    suspend fun insertTimer(timer: DeviceTimer) {
        unlockDao.insertTimer(timer)
    }

    suspend fun deleteTimer(timer: DeviceTimer) {
        unlockDao.deleteTimer(timer)
    }

    suspend fun deleteTimerById(id: Int) {
        unlockDao.deleteTimerById(id)
    }

    suspend fun setStepCompleted(stepKey: String, isCompleted: Boolean) {
        unlockDao.insertChecklistStep(ChecklistStep(stepKey, isCompleted))
    }
}
