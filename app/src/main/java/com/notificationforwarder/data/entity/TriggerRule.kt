package com.notificationforwarder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trigger_rules",
    foreignKeys = [
        ForeignKey(
            entity = WebhookConfig::class,
            parentColumns = ["id"],
            childColumns = ["webhookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("webhookId")]
)
data class TriggerRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val webhookId: Long,
    val packageNamePattern: String? = null, // null = match all
    val contentPattern: String? = null, // regex pattern for notification content
    val minPriority: Int = -2, // PRIORITY_MIN
    val maxPriority: Int = 2, // PRIORITY_MAX
    val enabled: Boolean = true
)

