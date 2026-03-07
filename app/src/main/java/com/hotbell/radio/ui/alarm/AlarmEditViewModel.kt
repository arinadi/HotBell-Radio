package com.hotbell.radio.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.alarms.AlarmRepository
import com.hotbell.radio.alarms.AlarmScheduler
import com.hotbell.radio.data.AlarmEntity
import com.hotbell.radio.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AlarmEditViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val alarmRepository = AlarmRepository(db.alarmDao(), AlarmScheduler(application))

    private val _hour = MutableStateFlow(7)
    val hour: StateFlow<Int> = _hour.asStateFlow()

    private val _minute = MutableStateFlow(0)
    val minute: StateFlow<Int> = _minute.asStateFlow()

    private val _daysOfWeek = MutableStateFlow(0)
    val daysOfWeek: StateFlow<Int> = _daysOfWeek.asStateFlow()

    private val _stationUuid = MutableStateFlow<String?>(null)
    val stationUuid: StateFlow<String?> = _stationUuid.asStateFlow()

    private val _stationName = MutableStateFlow<String?>(null)
    val stationName: StateFlow<String?> = _stationName.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private var editingAlarmId: String? = null

    fun loadAlarm(alarmId: String?) {
        if (alarmId == null) {
            _isLoaded.value = true
            return
        }
        if (editingAlarmId == alarmId) return // Prevent overwriting user edits when returning from station selection
        viewModelScope.launch {
            alarmRepository.getAlarmById(alarmId)?.let { alarm ->
                editingAlarmId = alarm.id
                _hour.value = alarm.timeHour
                _minute.value = alarm.timeMin
                _daysOfWeek.value = alarm.daysOfWeek
                _stationUuid.value = alarm.stationUuid
                _stationName.value = alarm.stationName
            }
            _isLoaded.value = true
        }
    }

    fun setTime(h: Int, m: Int) {
        _hour.value = h
        _minute.value = m
    }

    fun toggleDay(dayBit: Int) {
        _daysOfWeek.value = _daysOfWeek.value xor (1 shl dayBit)
    }

    fun setStation(uuid: String, name: String) {
        _stationUuid.value = uuid
        _stationName.value = name
    }

    fun saveAlarm(onDone: () -> Unit) {
        viewModelScope.launch {
            val alarm = AlarmEntity(
                id = editingAlarmId ?: UUID.randomUUID().toString(),
                timeHour = _hour.value,
                timeMin = _minute.value,
                daysOfWeek = _daysOfWeek.value,
                isEnabled = true,
                stationUuid = _stationUuid.value,
                stationName = _stationName.value
            )
            if (editingAlarmId != null) {
                alarmRepository.updateAlarm(alarm)
            } else {
                alarmRepository.createAlarm(alarm)
            }
            onDone()
        }
    }
}
