package com.notificationforwarder.data.model

data class NotificationData(
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val bigText: String?,
    val priority: Int,
    val timestamp: Long,
    val iconBase64: String?, // Notification icon or app icon as base64
    val key: String
)

