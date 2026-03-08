package com.hotbell.radio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_logs")
data class AlarmLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: String,
    val eventType: String, // "fired", "dismissed", "snoozed", "auto_dismissed"
    val timestamp: Long = System.currentTimeMillis(),
    val responseTimeMs: Long? = null // time from fired to dismissed (ms)
)
