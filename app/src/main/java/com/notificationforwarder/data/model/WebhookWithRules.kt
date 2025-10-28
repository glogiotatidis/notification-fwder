package com.notificationforwarder.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig

data class WebhookWithRules(
    @Embedded val webhook: WebhookConfig,
    @Relation(
        parentColumn = "id",
        entityColumn = "webhookId"
    )
    val rules: List<TriggerRule>
)

