package com.hotbell.radio.alarms

import com.hotbell.radio.data.AlarmDao
import com.hotbell.radio.data.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {

    fun getAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAll()

    suspend fun getAlarmById(id: String): AlarmEntity? = alarmDao.getById(id)

    suspend fun createAlarm(alarm: AlarmEntity) {
        alarmDao.insert(alarm)
        if (alarm.isEnabled) {
            alarmScheduler.schedule(alarm)
        }
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.update(alarm)
        if (alarm.isEnabled) {
            alarmScheduler.schedule(alarm)
        } else {
            alarmScheduler.cancel(alarm)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmScheduler.cancel(alarm)
        alarmDao.delete(alarm)
    }

    suspend fun toggleAlarm(alarm: AlarmEntity) {
        val updated = alarm.copy(isEnabled = !alarm.isEnabled)
        updateAlarm(updated)
    }
}
