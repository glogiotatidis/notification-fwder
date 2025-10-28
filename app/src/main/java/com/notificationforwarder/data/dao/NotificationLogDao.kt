package com.notificationforwarder.data.dao

import androidx.room.*
import com.notificationforwarder.data.entity.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY sentAt DESC LIMIT 1000")
    fun getAllFlow(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE success = :success ORDER BY sentAt DESC LIMIT 1000")
    fun getByStatusFlow(success: Boolean): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE packageName = :packageName ORDER BY sentAt DESC LIMIT 100")
    fun getByPackageFlow(packageName: String): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NotificationLog): Long

    @Query("SELECT COUNT(*) FROM notification_logs")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM notification_logs")
    fun getCountFlow(): Flow<Int>

    @Query("DELETE FROM notification_logs WHERE id IN (SELECT id FROM notification_logs ORDER BY sentAt DESC LIMIT -1 OFFSET 1000)")
    suspend fun pruneOldLogs()

    @Query("DELETE FROM notification_logs")
    suspend fun deleteAll()

    @Query("DELETE FROM notification_logs WHERE sentAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

