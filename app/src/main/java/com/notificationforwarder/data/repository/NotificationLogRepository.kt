package com.notificationforwarder.data.repository

import com.notificationforwarder.data.dao.NotificationLogDao
import com.notificationforwarder.data.entity.NotificationLog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationLogRepository @Inject constructor(
    private val notificationLogDao: NotificationLogDao
) {
    fun getAllLogs(): Flow<List<NotificationLog>> = notificationLogDao.getAllFlow()

    fun getLogsByStatus(success: Boolean): Flow<List<NotificationLog>> =
        notificationLogDao.getByStatusFlow(success)

    fun getLogsByPackage(packageName: String): Flow<List<NotificationLog>> =
        notificationLogDao.getByPackageFlow(packageName)

    suspend fun insertLog(log: NotificationLog): Long {
        val id = notificationLogDao.insert(log)
        // Auto-prune to keep only last 1000 logs
        val count = notificationLogDao.getCount()
        if (count > 1000) {
            notificationLogDao.pruneOldLogs()
        }
        return id
    }

    fun getLogCount(): Flow<Int> = notificationLogDao.getCountFlow()

    suspend fun clearAllLogs() = notificationLogDao.deleteAll()

    suspend fun deleteLogsOlderThan(timestamp: Long) = notificationLogDao.deleteOlderThan(timestamp)
}

