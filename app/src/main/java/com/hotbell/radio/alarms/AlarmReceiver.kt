package com.hotbell.radio.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hotbell.radio.R
import com.hotbell.radio.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "hotbell_alarm_channel"
        private const val CHANNEL_NAME = "HotBell Alarms"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("EXTRA_ALARM_ID") ?: return
        val stationUuid = intent.getStringExtra("EXTRA_STATION_UUID")
        val stationName = intent.getStringExtra("EXTRA_STATION_NAME")
        val stationUrl = intent.getStringExtra("EXTRA_STATION_URL")
        val snoozeDurationMin = intent.getIntExtra("EXTRA_SNOOZE_DURATION", 5)
        val maxSnoozeCount = intent.getIntExtra("EXTRA_MAX_SNOOZE", 3)
        val autoDismissMin = intent.getIntExtra("EXTRA_AUTO_DISMISS", 10)
        val dismissType = intent.getStringExtra("EXTRA_DISMISS_TYPE") ?: "math"
        val targetPhotoPath = intent.getStringExtra("EXTRA_TARGET_PHOTO_PATH")

        Log.d(TAG, "Alarm fired via Broadcast! id=$alarmId, station=$stationName")

        // Check skipNext from DB (uses goAsync for coroutine)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val alarm = db.alarmDao().getById(alarmId)
                if (alarm?.skipNext == true) {
                    Log.d(TAG, "Alarm $alarmId skipNext=true. Skipping & rescheduling.")
                    db.alarmDao().update(alarm.copy(skipNext = false))
                    AlarmScheduler(context).schedule(alarm.copy(skipNext = false))
                    pendingResult.finish()
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking skipNext", e)
            }

            // Fire the alarm
            fireAlarm(context, alarmId, stationUuid, stationName, stationUrl,
                snoozeDurationMin, maxSnoozeCount, autoDismissMin, dismissType, targetPhotoPath)
            pendingResult.finish()
        }
    }

    private fun fireAlarm(
        context: Context,
        alarmId: String,
        stationUuid: String?,
        stationName: String?,
        stationUrl: String?,
        snoozeDurationMin: Int,
        maxSnoozeCount: Int,
        autoDismissMin: Int,
        dismissType: String,
        targetPhotoPath: String?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Used to display high priority alarms" }
        notificationManager.createNotificationChannel(channel)

        val wakeUpIntent = Intent(context, com.hotbell.radio.ui.wakeup.WakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_STATION_UUID", stationUuid ?: "")
            putExtra("EXTRA_STATION_NAME", stationName ?: "Alarm Station")
            putExtra("EXTRA_STATION_URL", stationUrl ?: "")
            putExtra("EXTRA_SNOOZE_DURATION", snoozeDurationMin)
            putExtra("EXTRA_MAX_SNOOZE", maxSnoozeCount)
            putExtra("EXTRA_AUTO_DISMISS", autoDismissMin)
            putExtra("EXTRA_DISMISS_TYPE", dismissType)
            putExtra("EXTRA_TARGET_PHOTO_PATH", targetPhotoPath)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, alarmId.hashCode(), wakeUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("HotBell Alarm")
            .setContentText("Wake up! Playing ${stationName ?: "radio"}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)

        notificationManager.notify(alarmId.hashCode(), notification.build())
    }
}
