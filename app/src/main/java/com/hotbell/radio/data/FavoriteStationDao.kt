package com.hotbell.radio.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    @Query("SELECT * FROM favorite_stations ORDER BY name ASC")
    fun getAll(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT * FROM favorite_stations")
    suspend fun getAllOnce(): List<FavoriteStationEntity>

    @Query("SELECT * FROM favorite_stations WHERE stationUuid = :uuid")
    suspend fun getByUuid(uuid: String): FavoriteStationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStationEntity)

    @Delete
    suspend fun delete(station: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE stationUuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationUuid = :uuid)")
    suspend fun isFavorite(uuid: String): Boolean
}
