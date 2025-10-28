package com.notificationforwarder

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notificationforwarder.data.AppDatabase
import com.notificationforwarder.data.entity.NotificationLog
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndReadWebhookConfig() = runTest {
        val webhook = WebhookConfig(
            url = "https://test.com/webhook",
            headers = mapOf("Authorization" to "Bearer token"),
            enabled = true
        )

        val id = database.webhookConfigDao().insert(webhook)
        val retrieved = database.webhookConfigDao().getById(id)

        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.url).isEqualTo("https://test.com/webhook")
        assertThat(retrieved?.headers).containsEntry("Authorization", "Bearer token")
        assertThat(retrieved?.enabled).isTrue()
    }

    @Test
    fun webhookConfigFlowUpdates() = runTest {
        val webhook1 = WebhookConfig(url = "https://test1.com", enabled = true)
        val webhook2 = WebhookConfig(url = "https://test2.com", enabled = true)

        database.webhookConfigDao().insert(webhook1)
        val firstFlow = database.webhookConfigDao().getAllFlow().first()
        assertThat(firstFlow).hasSize(1)

        database.webhookConfigDao().insert(webhook2)
        val secondFlow = database.webhookConfigDao().getAllFlow().first()
        assertThat(secondFlow).hasSize(2)
    }

    @Test
    fun insertTriggerRuleWithForeignKey() = runTest {
        val webhook = WebhookConfig(url = "https://test.com", enabled = true)
        val webhookId = database.webhookConfigDao().insert(webhook)

        val triggerRule = TriggerRule(
            webhookId = webhookId,
            packageNamePattern = "com.example",
            contentPattern = ".*test.*",
            minPriority = -1,
            maxPriority = 1,
            enabled = true
        )

        val ruleId = database.triggerRuleDao().insert(triggerRule)
        val rules = database.triggerRuleDao().getByWebhookId(webhookId)

        assertThat(rules).hasSize(1)
        assertThat(rules[0].id).isEqualTo(ruleId)
        assertThat(rules[0].packageNamePattern).isEqualTo("com.example")
    }

    @Test
    fun deletingWebhookCascadesDeleteTriggerRules() = runTest {
        val webhook = WebhookConfig(url = "https://test.com", enabled = true)
        val webhookId = database.webhookConfigDao().insert(webhook)

        val triggerRule = TriggerRule(webhookId = webhookId, enabled = true)
        database.triggerRuleDao().insert(triggerRule)

        val rulesBeforeDelete = database.triggerRuleDao().getByWebhookId(webhookId)
        assertThat(rulesBeforeDelete).hasSize(1)

        database.webhookConfigDao().deleteById(webhookId)

        val rulesAfterDelete = database.triggerRuleDao().getByWebhookId(webhookId)
        assertThat(rulesAfterDelete).isEmpty()
    }

    @Test
    fun insertNotificationLog() = runTest {
        val log = NotificationLog(
            webhookId = 1,
            webhookUrl = "https://test.com",
            packageName = "com.test",
            appName = "Test App",
            title = "Test",
            text = "Test text",
            priority = 0,
            timestamp = System.currentTimeMillis(),
            iconBase64 = "base64icon",
            httpStatusCode = 200,
            success = true
        )

        val id = database.notificationLogDao().insert(log)
        val logs = database.notificationLogDao().getAllFlow().first()

        assertThat(logs).hasSize(1)
        assertThat(logs[0].id).isEqualTo(id)
        assertThat(logs[0].iconBase64).isEqualTo("base64icon")
        assertThat(logs[0].success).isTrue()
    }

    @Test
    fun notificationLogPruning() = runTest {
        // Insert 1010 logs
        repeat(1010) { index ->
            val log = NotificationLog(
                webhookId = 1,
                webhookUrl = "https://test.com",
                packageName = "com.test",
                appName = "Test",
                title = "Log $index",
                text = "Text",
                priority = 0,
                timestamp = System.currentTimeMillis(),
                success = true,
                sentAt = System.currentTimeMillis() - (1010 - index) * 1000
            )
            database.notificationLogDao().insert(log)
        }

        // Manually trigger pruning
        database.notificationLogDao().pruneOldLogs()

        val count = database.notificationLogDao().getCount()
        assertThat(count).isAtMost(1000)
    }

    @Test
    fun filterNotificationLogsByStatus() = runTest {
        val successLog = NotificationLog(
            webhookId = 1,
            webhookUrl = "https://test.com",
            packageName = "com.test",
            appName = "Test",
            title = "Success",
            text = "Text",
            priority = 0,
            timestamp = System.currentTimeMillis(),
            success = true
        )

        val failedLog = NotificationLog(
            webhookId = 1,
            webhookUrl = "https://test.com",
            packageName = "com.test",
            appName = "Test",
            title = "Failed",
            text = "Text",
            priority = 0,
            timestamp = System.currentTimeMillis(),
            success = false,
            errorMessage = "Connection failed"
        )

        database.notificationLogDao().insert(successLog)
        database.notificationLogDao().insert(failedLog)

        val successLogs = database.notificationLogDao().getByStatusFlow(true).first()
        val failedLogs = database.notificationLogDao().getByStatusFlow(false).first()

        assertThat(successLogs).hasSize(1)
        assertThat(successLogs[0].title).isEqualTo("Success")

        assertThat(failedLogs).hasSize(1)
        assertThat(failedLogs[0].title).isEqualTo("Failed")
        assertThat(failedLogs[0].errorMessage).isEqualTo("Connection failed")
    }

    @Test
    fun getEnabledWebhooksOnly() = runTest {
        val enabled = WebhookConfig(url = "https://enabled.com", enabled = true)
        val disabled = WebhookConfig(url = "https://disabled.com", enabled = false)

        database.webhookConfigDao().insert(enabled)
        database.webhookConfigDao().insert(disabled)

        val enabledWebhooks = database.webhookConfigDao().getEnabledFlow().first()

        assertThat(enabledWebhooks).hasSize(1)
        assertThat(enabledWebhooks[0].url).isEqualTo("https://enabled.com")
    }

    @Test
    fun updateWebhookConfig() = runTest {
        val webhook = WebhookConfig(url = "https://test.com", enabled = true)
        val id = database.webhookConfigDao().insert(webhook)

        val updated = webhook.copy(id = id, enabled = false, url = "https://updated.com")
        database.webhookConfigDao().update(updated)

        val retrieved = database.webhookConfigDao().getById(id)
        assertThat(retrieved?.enabled).isFalse()
        assertThat(retrieved?.url).isEqualTo("https://updated.com")
    }
}

