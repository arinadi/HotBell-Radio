package com.hotbell.radio.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

object AlarmUtils {

    fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeek: Int): Long {
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Use real current time for comparison to ensure strictly future triggers
        val now = Calendar.getInstance()
        
        if (daysOfWeek == 0) {
            // One-time alarm: if time already passed today, set for tomorrow
            if (!alarm.after(now)) {
                alarm.add(Calendar.DAY_OF_MONTH, 1)
            }
            return alarm.timeInMillis
        }

        // Repeating alarm: find the next matching day
        for (i in 0..14) { // Look further ahead to be safe (though 7 is enough)
            val candidate = Calendar.getInstance().apply {
                timeInMillis = alarm.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }
            // Calendar: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
            // Our bitmask: bit 0=Sunday, bit 1=Monday, ..., bit 6=Saturday
            val dayBit = candidate.get(Calendar.DAY_OF_WEEK) - 1
            if (daysOfWeek and (1 shl dayBit) != 0) {
                // If today, candidate must be strictly in the future
                if (i == 0 && !candidate.after(now)) continue
                return candidate.timeInMillis
            }
        }

        // Fallback: set for tomorrow
        alarm.add(Calendar.DAY_OF_MONTH, 1)
        return alarm.timeInMillis
    }

    fun formatCountdown(triggerTime: Long): String {
        val now = System.currentTimeMillis()
        val remaining = triggerTime - now
        
        if (remaining <= 0) return "00:00:00"

        val hours = TimeUnit.MILLISECONDS.toHours(remaining)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
