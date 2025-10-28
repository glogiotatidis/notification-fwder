package com.notificationforwarder.webhook

data class WebhookPayload(
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val bigText: String?,
    val priority: Int,
    val timestamp: Long,
    val iconBase64: String?, // Notification icon or app icon as base64
    val device: DeviceInfo
)

data class DeviceInfo(
    val androidVersion: String,
    val deviceModel: String,
    val deviceManufacturer: String
)

