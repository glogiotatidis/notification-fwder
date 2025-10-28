package com.notificationforwarder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val webhookId: Long,
    val webhookUrl: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val priority: Int,
    val timestamp: Long,
    val iconBase64: String? = null, // Notification icon or app icon as base64
    val httpStatusCode: Int? = null,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val responseBody: String? = null,
    val sentAt: Long = System.currentTimeMillis()
)

