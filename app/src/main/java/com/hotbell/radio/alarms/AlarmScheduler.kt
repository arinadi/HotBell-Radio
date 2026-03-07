package com.hotbell.radio.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.hotbell.radio.data.AlarmEntity
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    companion object {
        private const val TAG = "AlarmScheduler"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancel(alarm)
            return
        }

        val triggerTime = calculateNextTriggerTime(alarm.timeHour, alarm.timeMin, alarm.daysOfWeek)
        val pendingIntent = createPendingIntent(alarm)

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
            pendingIntent
        )

        Log.d(TAG, "Scheduled alarm ${alarm.id} for ${alarm.timeHour}:${String.format("%02d", alarm.timeMin)} (trigger: $triggerTime)")
    }

    fun cancel(alarm: AlarmEntity) {
        val pendingIntent = createPendingIntent(alarm)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled alarm ${alarm.id}")
    }

    private fun createPendingIntent(alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, com.hotbell.radio.ui.wakeup.WakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_ALARM_ID", alarm.id)
            putExtra("EXTRA_STATION_UUID", alarm.stationUuid)
            putExtra("EXTRA_STATION_NAME", alarm.stationName)
        }
        return PendingIntent.getActivity(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeek: Int): Long {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (daysOfWeek == 0) {
            // One-time alarm: if time already passed today, set for tomorrow
            if (alarm.before(now) || alarm == now) {
                alarm.add(Calendar.DAY_OF_MONTH, 1)
            }
            return alarm.timeInMillis
        }

        // Repeating alarm: find the next matching day
        for (i in 0..6) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = alarm.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }
            // Calendar: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
            // Our bitmask: bit 0=Sunday, bit 1=Monday, ..., bit 6=Saturday
            val dayBit = candidate.get(Calendar.DAY_OF_WEEK) - 1
            if (daysOfWeek and (1 shl dayBit) != 0) {
                if (i == 0 && candidate.before(now)) continue
                return candidate.timeInMillis
            }
        }

        // Fallback: set for tomorrow
        alarm.add(Calendar.DAY_OF_MONTH, 1)
        return alarm.timeInMillis
    }
}
