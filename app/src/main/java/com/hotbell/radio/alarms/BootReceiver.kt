package com.hotbell.radio.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hotbell.radio.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Boot completed. Rescheduling alarms...")

        val db = AppDatabase.getInstance(context)
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            val enabledAlarms = db.alarmDao().getEnabled()
            Log.d(TAG, "Found ${enabledAlarms.size} enabled alarms to reschedule")
            enabledAlarms.forEach { alarm ->
                scheduler.schedule(alarm)
            }
        }
    }
}
