package com.hotbell.radio.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID") ?: return
        val stationUuid = intent.getStringExtra("STATION_UUID")
        val stationName = intent.getStringExtra("STATION_NAME")

        Log.d(TAG, "Alarm fired! id=$alarmId, station=$stationName")

        // Retrieve URL from DB since Intent only has UUID
        val db = com.hotbell.radio.data.AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val stationUrl = stationUuid?.let { db.favoriteStationDao().getByUuid(it)?.urlResolved }
            
            val wakeUpIntent = Intent(context, com.hotbell.radio.ui.wakeup.WakeUpActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_ALARM_ID", alarmId)
                putExtra("EXTRA_STATION_UUID", stationUuid ?: "")
                putExtra("EXTRA_STATION_NAME", stationName ?: "Alarm Station")
                putExtra("EXTRA_STATION_URL", stationUrl ?: "")
            }
            context.startActivity(wakeUpIntent)
        }
    }
}
