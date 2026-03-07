package com.hotbell.radio.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.alarms.AlarmRepository
import com.hotbell.radio.alarms.AlarmScheduler
import com.hotbell.radio.data.AlarmEntity
import com.hotbell.radio.data.AppDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val alarmRepository = AlarmRepository(db.alarmDao(), AlarmScheduler(application))

    private val _currentTime = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    val alarms = alarmRepository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites = db.favoriteStationDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarmCountdowns = combine(alarms, _currentTime) { alarmList, _ ->
        alarmList.filter { it.isEnabled }.associate { alarm ->
            val triggerTime = com.hotbell.radio.utils.AlarmUtils.calculateNextTriggerTime(
                alarm.timeHour,
                alarm.timeMin,
                alarm.daysOfWeek
            )
            alarm.id to com.hotbell.radio.utils.AlarmUtils.formatCountdown(triggerTime)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.toggleAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
        }
    }

    fun playFavoriteStation(station: com.hotbell.radio.data.FavoriteStationEntity) {
        com.hotbell.radio.player.RadioPlayerManager.play(getApplication(), station.urlResolved, station.name)
    }

    fun stopRadio() {
        com.hotbell.radio.player.RadioPlayerManager.stop(getApplication())
    }
}
