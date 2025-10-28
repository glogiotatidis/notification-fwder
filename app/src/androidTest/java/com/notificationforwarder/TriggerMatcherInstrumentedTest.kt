package com.notificationforwarder

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.model.NotificationData
import com.notificationforwarder.webhook.TriggerMatcher
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TriggerMatcherInstrumentedTest {

    private lateinit var triggerMatcher: TriggerMatcher

    @Before
    fun setup() {
        triggerMatcher = TriggerMatcher()
    }

    @Test
    fun matchesComplexRegexPatterns() {
        val notification = NotificationData(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "John Doe",
            text = "Call me when you get this urgent message!",
            subText = null,
            bigText = null,
            priority = 1,
            timestamp = System.currentTimeMillis(),
            iconBase64 = null,
            key = "test"
        )

        val rule = TriggerRule(
            webhookId = 1,
            packageNamePattern = "whatsapp",
            contentPattern = ".*(urgent|important|asap).*",
            minPriority = 0,
            maxPriority = 2,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun handlesMultipleRulesCorrectly() {
        val notification = NotificationData(
            packageName = "com.example.app",
            appName = "Example",
            title = "Test",
            text = "Message",
            subText = null,
            bigText = null,
            priority = 0,
            timestamp = System.currentTimeMillis(),
            iconBase64 = null,
            key = "test"
        )

        val rules = listOf(
            TriggerRule(webhookId = 1, packageNamePattern = "com.other", enabled = true),
            TriggerRule(webhookId = 2, packageNamePattern = "example", enabled = true),
            TriggerRule(webhookId = 3, packageNamePattern = "another", enabled = false)
        )

        val result = triggerMatcher.matches(notification, rules)

        assertThat(result).isTrue()
    }

    @Test
    fun matchesOnlyWhenAllConditionsMet() {
        val notification = NotificationData(
            packageName = "com.banking.app",
            appName = "Bank App",
            title = "Security Alert",
            text = "Login attempt detected",
            subText = null,
            bigText = null,
            priority = 2,
            timestamp = System.currentTimeMillis(),
            iconBase64 = null,
            key = "test"
        )

        val strictRule = TriggerRule(
            webhookId = 1,
            packageNamePattern = "banking",
            contentPattern = ".*(security|alert|fraud).*",
            minPriority = 1,
            maxPriority = 2,
            enabled = true
        )

        assertThat(triggerMatcher.matches(notification, strictRule)).isTrue()

        val tooStrictRule = strictRule.copy(minPriority = 3)
        assertThat(triggerMatcher.matches(notification, tooStrictRule)).isFalse()
    }
}

