package com.hotbell.radio.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AlarmEntity::class, FavoriteStationEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    // Daos will be added in later modules.
}
