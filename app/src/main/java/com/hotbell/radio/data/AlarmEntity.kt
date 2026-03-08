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
    val stationName: String?,
    val stationUrl: String?,
    val label: String? = "",
    val isVibrateEnabled: Boolean = true,
    val snoozeDurationMin: Int = 5,
    val maxSnoozeCount: Int = 3,
    val autoDismissMin: Int = 10,
    val skipNext: Boolean = false,
    
    // Phase 7: Photo Match Challenge
    val dismissType: String = "math", // "math" or "photo"
    val targetPhotoPath: String? = null
)
