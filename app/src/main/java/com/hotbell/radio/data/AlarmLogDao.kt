package com.hotbell.radio.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmLogDao {

    @Insert
    suspend fun insert(log: AlarmLogEntity)

    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AlarmLogEntity>>

    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<AlarmLogEntity>>

    @Query("SELECT COUNT(*) FROM alarm_logs WHERE eventType = :type")
    suspend fun countByType(type: String): Int

    @Query("SELECT AVG(responseTimeMs) FROM alarm_logs WHERE eventType = 'dismissed' AND responseTimeMs IS NOT NULL")
    suspend fun avgDismissTimeMs(): Double?

    @Query("SELECT COUNT(*) FROM alarm_logs WHERE eventType = 'dismissed' AND timestamp >= :since")
    suspend fun dismissedSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM alarm_logs WHERE eventType = 'snoozed' AND timestamp >= :since")
    suspend fun snoozedSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM alarm_logs WHERE eventType = 'fired' AND timestamp >= :since")
    suspend fun firedSince(since: Long): Int
}
