package com.hotbell.radio.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlarmStats(
    val totalFired: Int = 0,
    val totalDismissed: Int = 0,
    val totalSnoozed: Int = 0,
    val totalAutoDismissed: Int = 0,
    val avgDismissTimeSec: Int = 0,
    val last7DaysFired: Int = 0,
    val last7DaysDismissed: Int = 0,
    val last7DaysSnoozed: Int = 0,
    val dismissRate: Int = 0 // percentage
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmLogDao = AppDatabase.getInstance(application).alarmLogDao()

    private val _stats = MutableStateFlow(AlarmStats())
    val stats: StateFlow<AlarmStats> = _stats.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val fired = alarmLogDao.countByType("fired")
            val dismissed = alarmLogDao.countByType("dismissed")
            val snoozed = alarmLogDao.countByType("snoozed")
            val autoDismissed = alarmLogDao.countByType("auto_dismissed")
            val avgMs = alarmLogDao.avgDismissTimeMs() ?: 0.0

            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val weekFired = alarmLogDao.firedSince(sevenDaysAgo)
            val weekDismissed = alarmLogDao.dismissedSince(sevenDaysAgo)
            val weekSnoozed = alarmLogDao.snoozedSince(sevenDaysAgo)

            _stats.value = AlarmStats(
                totalFired = fired,
                totalDismissed = dismissed,
                totalSnoozed = snoozed,
                totalAutoDismissed = autoDismissed,
                avgDismissTimeSec = (avgMs / 1000).toInt(),
                last7DaysFired = weekFired,
                last7DaysDismissed = weekDismissed,
                last7DaysSnoozed = weekSnoozed,
                dismissRate = if (fired > 0) ((dismissed * 100) / fired) else 0
            )
        }
    }
}
