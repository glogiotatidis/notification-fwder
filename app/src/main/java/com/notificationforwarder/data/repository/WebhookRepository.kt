package com.notificationforwarder.data.repository

import com.notificationforwarder.data.dao.TriggerRuleDao
import com.notificationforwarder.data.dao.WebhookConfigDao
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig
import com.notificationforwarder.data.model.WebhookWithRules
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebhookRepository @Inject constructor(
    private val webhookConfigDao: WebhookConfigDao,
    private val triggerRuleDao: TriggerRuleDao
) {
    fun getAllWebhooks(): Flow<List<WebhookConfig>> = webhookConfigDao.getAllFlow()

    fun getEnabledWebhooks(): Flow<List<WebhookConfig>> = webhookConfigDao.getEnabledFlow()

    fun getAllWebhooksWithRules(): Flow<List<WebhookWithRules>> = webhookConfigDao.getAllWithRulesFlow()

    suspend fun getWebhookById(id: Long): WebhookConfig? = webhookConfigDao.getById(id)

    suspend fun getWebhookWithRules(id: Long): WebhookWithRules? = webhookConfigDao.getWebhookWithRules(id)

    suspend fun insertWebhook(webhook: WebhookConfig): Long = webhookConfigDao.insert(webhook)

    suspend fun updateWebhook(webhook: WebhookConfig) = webhookConfigDao.update(webhook)

    suspend fun deleteWebhook(webhook: WebhookConfig) = webhookConfigDao.delete(webhook)

    suspend fun deleteWebhookById(id: Long) = webhookConfigDao.deleteById(id)

    fun getWebhookCount(): Flow<Int> = webhookConfigDao.getCountFlow()

    // Trigger Rules
    fun getTriggerRulesByWebhookId(webhookId: Long): Flow<List<TriggerRule>> =
        triggerRuleDao.getByWebhookIdFlow(webhookId)

    suspend fun getTriggerRulesSync(webhookId: Long): List<TriggerRule> =
        triggerRuleDao.getByWebhookId(webhookId)

    suspend fun getAllEnabledTriggerRules(): List<TriggerRule> = triggerRuleDao.getAllEnabled()

    suspend fun insertTriggerRule(rule: TriggerRule): Long = triggerRuleDao.insert(rule)

    suspend fun updateTriggerRule(rule: TriggerRule) = triggerRuleDao.update(rule)

    suspend fun deleteTriggerRule(rule: TriggerRule) = triggerRuleDao.delete(rule)

    suspend fun deleteTriggerRuleById(ruleId: Long) {
        // Delete by creating a temporary object with the ID
        val rule = TriggerRule(id = ruleId, webhookId = 0)
        triggerRuleDao.delete(rule)
    }

    suspend fun deleteTriggerRulesByWebhookId(webhookId: Long) =
        triggerRuleDao.deleteByWebhookId(webhookId)
}

