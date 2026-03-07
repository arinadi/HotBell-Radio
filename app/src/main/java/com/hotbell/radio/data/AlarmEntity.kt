package com.hotbell.radio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: String,
    val timeHour: Int,
    val timeMin: Int,
    val daysOfWeek: Int, // Bitmask
    val isEnabled: Boolean,
    val stationUuid: String?,
    val stationName: String?
)
