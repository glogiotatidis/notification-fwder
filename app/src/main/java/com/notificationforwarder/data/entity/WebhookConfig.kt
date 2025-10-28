package com.notificationforwarder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.notificationforwarder.data.converter.MapTypeConverter

@Entity(tableName = "webhook_configs")
@TypeConverters(MapTypeConverter::class)
data class WebhookConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

