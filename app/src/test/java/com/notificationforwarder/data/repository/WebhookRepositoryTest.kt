package com.notificationforwarder.data.repository

import com.notificationforwarder.data.dao.TriggerRuleDao
import com.notificationforwarder.data.dao.WebhookConfigDao
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WebhookRepositoryTest {

    private lateinit var webhookRepository: WebhookRepository
    private val webhookConfigDao: WebhookConfigDao = mockk()
    private val triggerRuleDao: TriggerRuleDao = mockk()

    @Before
    fun setup() {
        webhookRepository = WebhookRepository(webhookConfigDao, triggerRuleDao)
    }

    @Test
    fun `getAllWebhooks returns flow from dao`() = runTest {
        val webhooks = listOf(
            WebhookConfig(id = 1, url = "https://example.com/webhook1"),
            WebhookConfig(id = 2, url = "https://example.com/webhook2")
        )
        every { webhookConfigDao.getAllFlow() } returns flowOf(webhooks)

        val result = webhookRepository.getAllWebhooks().first()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[1].id).isEqualTo(2)
        verify { webhookConfigDao.getAllFlow() }
    }

    @Test
    fun `getEnabledWebhooks returns only enabled webhooks`() = runTest {
        val enabledWebhooks = listOf(
            WebhookConfig(id = 1, url = "https://example.com/webhook1", enabled = true)
        )
        every { webhookConfigDao.getEnabledFlow() } returns flowOf(enabledWebhooks)

        val result = webhookRepository.getEnabledWebhooks().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].enabled).isTrue()
        verify { webhookConfigDao.getEnabledFlow() }
    }

    @Test
    fun `getWebhookById returns webhook when exists`() = runTest {
        val webhook = WebhookConfig(id = 1, url = "https://example.com/webhook")
        coEvery { webhookConfigDao.getById(1) } returns webhook

        val result = webhookRepository.getWebhookById(1)

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1)
        coVerify { webhookConfigDao.getById(1) }
    }

    @Test
    fun `getWebhookById returns null when not exists`() = runTest {
        coEvery { webhookConfigDao.getById(999) } returns null

        val result = webhookRepository.getWebhookById(999)

        assertThat(result).isNull()
        coVerify { webhookConfigDao.getById(999) }
    }

    @Test
    fun `insertWebhook calls dao insert`() = runTest {
        val webhook = WebhookConfig(url = "https://example.com/webhook")
        coEvery { webhookConfigDao.insert(webhook) } returns 1L

        val id = webhookRepository.insertWebhook(webhook)

        assertThat(id).isEqualTo(1L)
        coVerify { webhookConfigDao.insert(webhook) }
    }

    @Test
    fun `updateWebhook calls dao update`() = runTest {
        val webhook = WebhookConfig(id = 1, url = "https://example.com/webhook")
        coEvery { webhookConfigDao.update(webhook) } just Runs

        webhookRepository.updateWebhook(webhook)

        coVerify { webhookConfigDao.update(webhook) }
    }

    @Test
    fun `deleteWebhook calls dao delete`() = runTest {
        val webhook = WebhookConfig(id = 1, url = "https://example.com/webhook")
        coEvery { webhookConfigDao.delete(webhook) } just Runs

        webhookRepository.deleteWebhook(webhook)

        coVerify { webhookConfigDao.delete(webhook) }
    }

    @Test
    fun `insertTriggerRule calls dao insert`() = runTest {
        val rule = TriggerRule(webhookId = 1, packageNamePattern = "com.example")
        coEvery { triggerRuleDao.insert(rule) } returns 1L

        val id = webhookRepository.insertTriggerRule(rule)

        assertThat(id).isEqualTo(1L)
        coVerify { triggerRuleDao.insert(rule) }
    }

    @Test
    fun `getTriggerRulesSync returns rules for webhook`() = runTest {
        val rules = listOf(
            TriggerRule(id = 1, webhookId = 1, packageNamePattern = "com.example")
        )
        coEvery { triggerRuleDao.getByWebhookId(1) } returns rules

        val result = webhookRepository.getTriggerRulesSync(1)

        assertThat(result).hasSize(1)
        assertThat(result[0].webhookId).isEqualTo(1)
        coVerify { triggerRuleDao.getByWebhookId(1) }
    }

    @Test
    fun `getAllEnabledTriggerRules returns only enabled rules`() = runTest {
        val rules = listOf(
            TriggerRule(id = 1, webhookId = 1, enabled = true),
            TriggerRule(id = 2, webhookId = 2, enabled = true)
        )
        coEvery { triggerRuleDao.getAllEnabled() } returns rules

        val result = webhookRepository.getAllEnabledTriggerRules()

        assertThat(result).hasSize(2)
        assertThat(result.all { it.enabled }).isTrue()
        coVerify { triggerRuleDao.getAllEnabled() }
    }
}

