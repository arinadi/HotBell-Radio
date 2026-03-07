package com.hotbell.radio.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hotbell.radio.data.AlarmEntity
import com.hotbell.radio.utils.AlarmUtils

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

        val triggerTime = AlarmUtils.calculateNextTriggerTime(alarm.timeHour, alarm.timeMin, alarm.daysOfWeek)
        val pendingIntent = createPendingIntent(alarm)

        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
            Log.d(TAG, "Scheduled alarm ${alarm.id} for ${alarm.timeHour}:${String.format("%02d", alarm.timeMin)} (trigger: $triggerTime)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm. Missing SCHEDULE_EXACT_ALARM permission.", e)
        }
    }

    fun cancel(alarm: AlarmEntity) {
        val pendingIntent = createPendingIntent(alarm)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled alarm ${alarm.id}")
    }

    private fun createPendingIntent(alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, com.hotbell.radio.alarms.AlarmReceiver::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarm.id)
            putExtra("EXTRA_STATION_UUID", alarm.stationUuid)
            putExtra("EXTRA_STATION_NAME", alarm.stationName)
            putExtra("EXTRA_STATION_URL", alarm.stationUrl)
            putExtra("EXTRA_SNOOZE_DURATION", alarm.snoozeDurationMin)
            putExtra("EXTRA_MAX_SNOOZE", alarm.maxSnoozeCount)
            putExtra("EXTRA_AUTO_DISMISS", alarm.autoDismissMin)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
