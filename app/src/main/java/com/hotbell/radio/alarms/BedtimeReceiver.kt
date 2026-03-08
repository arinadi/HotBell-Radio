package com.hotbell.radio.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hotbell.radio.MainActivity

class BedtimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("BedtimeReceiver", "Received bedtime reminder")
        showBedtimeNotification(context)

        // Reschedule for next day by triggering the ViewModel or just hitting the scheduler
        val prefs = context.getSharedPreferences("hotbell_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("bedtime_enabled", false)) {
            val hour = prefs.getInt("bedtime_hour", 22)
            val min = prefs.getInt("bedtime_minute", 0)
            BedtimeScheduler.schedule(context, hour, min)
        }
    }

    private fun showBedtimeNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bedtime_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bedtime Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to go to sleep"
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            // Use standard icon for now until a vector is generated
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time to wind down \uD83C\uDF19")
            .setContentText("It's almost your bedtime. Get ready for sleep to ensure you wake up fresh!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(54321, builder.build())
    }
}
