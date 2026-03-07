package com.hotbell.radio.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hotbell.radio.R

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

        Log.d(TAG, "Alarm fired via Broadcast! id=$alarmId, station=$stationName")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used to display high priority alarms"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val wakeUpIntent = Intent(context, com.hotbell.radio.ui.wakeup.WakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_STATION_UUID", stationUuid ?: "")
            putExtra("EXTRA_STATION_NAME", stationName ?: "Alarm Station")
            putExtra("EXTRA_STATION_URL", stationUrl ?: "")
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            wakeUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("HotBell Alarm")
            .setContentText("Wake up! Playing ${stationName ?: "radio"}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)

        notificationManager.notify(alarmId.hashCode(), notificationBuilder.build())
    }
}
