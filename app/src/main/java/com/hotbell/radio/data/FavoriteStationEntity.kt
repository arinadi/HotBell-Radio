package com.hotbell.radio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey val stationUuid: String,
    val name: String,
    val urlResolved: String,
    val favicon: String,
    val codec: String
)
