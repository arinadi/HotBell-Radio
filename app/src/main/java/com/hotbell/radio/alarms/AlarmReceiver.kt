package com.hotbell.radio.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID") ?: return
        val stationUuid = intent.getStringExtra("STATION_UUID")
        val stationName = intent.getStringExtra("STATION_NAME")

        Log.d(TAG, "Alarm fired! id=$alarmId, station=$stationName")

        // Launch WakeUpActivity (will be created in Module 4)
        // For now, log the trigger and launch a placeholder
        val wakeUpIntent = Intent().apply {
            setClassName(context.packageName, "com.hotbell.radio.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("STATION_UUID", stationUuid)
            putExtra("STATION_NAME", stationName)
            putExtra("IS_ALARM_TRIGGER", true)
        }
        context.startActivity(wakeUpIntent)
    }
}
