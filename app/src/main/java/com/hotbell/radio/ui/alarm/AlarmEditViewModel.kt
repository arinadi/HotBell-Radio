package com.hotbell.radio.ui.alarm

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.alarms.AlarmRepository
import com.hotbell.radio.alarms.AlarmScheduler
import com.hotbell.radio.data.AlarmEntity
import com.hotbell.radio.data.AppDatabase
import com.hotbell.radio.ui.wakeup.WakeUpActivity
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

    private val _stationUrl = MutableStateFlow<String?>(null)
    val stationUrl: StateFlow<String?> = _stationUrl.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _isVibrateEnabled = MutableStateFlow(true)
    val isVibrateEnabled: StateFlow<Boolean> = _isVibrateEnabled.asStateFlow()

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
                _stationUrl.value = alarm.stationUrl
                _label.value = alarm.label ?: ""
                _isVibrateEnabled.value = alarm.isVibrateEnabled
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

    fun setStation(uuid: String, name: String, url: String) {
        _stationUuid.value = uuid
        _stationName.value = name
        _stationUrl.value = url
    }

    fun setLabel(text: String) {
        _label.value = text
    }

    fun setVibrate(enabled: Boolean) {
        _isVibrateEnabled.value = enabled
    }

    fun testAlarm(context: Context) {
        val intent = Intent(context, WakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_ALARM_ID", editingAlarmId ?: "TEST_ALARM")
            putExtra("EXTRA_STATION_UUID", _stationUuid.value)
            putExtra("EXTRA_STATION_NAME", _stationName.value)
            putExtra("EXTRA_STATION_URL", _stationUrl.value)
        }
        context.startActivity(intent)
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
                stationName = _stationName.value,
                stationUrl = _stationUrl.value,
                label = _label.value,
                isVibrateEnabled = _isVibrateEnabled.value
            )
            if (editingAlarmId != null) {
                alarmRepository.updateAlarm(alarm)
            } else {
                alarmRepository.createAlarm(alarm)
            }
            onDone()
        }
    }

    fun deleteAlarm(onDone: () -> Unit) {
        val id = editingAlarmId ?: return
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(id) ?: return@launch
            alarmRepository.deleteAlarm(alarm)
            onDone()
        }
    }
}
