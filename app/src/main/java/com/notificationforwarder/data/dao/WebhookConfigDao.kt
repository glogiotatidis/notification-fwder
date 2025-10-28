package com.notificationforwarder.data.dao

import androidx.room.*
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig
import com.notificationforwarder.data.model.WebhookWithRules
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookConfigDao {
    @Query("SELECT * FROM webhook_configs ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<WebhookConfig>>

    @Query("SELECT * FROM webhook_configs WHERE enabled = 1 ORDER BY createdAt DESC")
    fun getEnabledFlow(): Flow<List<WebhookConfig>>

    @Query("SELECT * FROM webhook_configs WHERE id = :id")
    suspend fun getById(id: Long): WebhookConfig?

    @Transaction
    @Query("SELECT * FROM webhook_configs WHERE id = :id")
    suspend fun getWebhookWithRules(id: Long): WebhookWithRules?

    @Transaction
    @Query("SELECT * FROM webhook_configs ORDER BY createdAt DESC")
    fun getAllWithRulesFlow(): Flow<List<WebhookWithRules>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(webhook: WebhookConfig): Long

    @Update
    suspend fun update(webhook: WebhookConfig)

    @Delete
    suspend fun delete(webhook: WebhookConfig)

    @Query("DELETE FROM webhook_configs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM webhook_configs")
    fun getCountFlow(): Flow<Int>
}

@Dao
interface TriggerRuleDao {
    @Query("SELECT * FROM trigger_rules WHERE webhookId = :webhookId")
    fun getByWebhookIdFlow(webhookId: Long): Flow<List<TriggerRule>>

    @Query("SELECT * FROM trigger_rules WHERE webhookId = :webhookId")
    suspend fun getByWebhookId(webhookId: Long): List<TriggerRule>

    @Query("SELECT * FROM trigger_rules WHERE enabled = 1")
    suspend fun getAllEnabled(): List<TriggerRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: TriggerRule): Long

    @Update
    suspend fun update(rule: TriggerRule)

    @Delete
    suspend fun delete(rule: TriggerRule)

    @Query("DELETE FROM trigger_rules WHERE webhookId = :webhookId")
    suspend fun deleteByWebhookId(webhookId: Long)
}

