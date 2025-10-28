package com.notificationforwarder.webhook

import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.model.NotificationData
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class TriggerMatcherTest {

    private lateinit var triggerMatcher: TriggerMatcher

    @Before
    fun setup() {
        triggerMatcher = TriggerMatcher()
    }

    @Test
    fun `matches returns false when rule is disabled`() {
        val notification = createTestNotification()
        val rule = TriggerRule(
            webhookId = 1,
            enabled = false
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches returns true when package name matches`() {
        val notification = createTestNotification(packageName = "com.example.app")
        val rule = TriggerRule(
            webhookId = 1,
            packageNamePattern = "com.example",
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns false when package name does not match`() {
        val notification = createTestNotification(packageName = "com.other.app")
        val rule = TriggerRule(
            webhookId = 1,
            packageNamePattern = "com.example",
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches returns true when content matches simple text pattern`() {
        val notification = createTestNotification(
            title = "Urgent message",
            text = "This is urgent!"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "urgent",
            useRegex = false,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns false when content does not match simple text pattern`() {
        val notification = createTestNotification(
            title = "Regular message",
            text = "Normal content"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "urgent",
            useRegex = false,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches returns true when content matches regex pattern`() {
        val notification = createTestNotification(
            title = "Urgent message",
            text = "This is urgent!"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = ".*urgent.*",
            useRegex = true,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns false when content does not match regex pattern`() {
        val notification = createTestNotification(
            title = "Regular message",
            text = "Normal content"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = ".*urgent.*",
            useRegex = true,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `simple text matching works with partial matches`() {
        val notification = createTestNotification(
            title = "Important notification",
            text = "Please review this"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "import",
            useRegex = false,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `regex matching requires exact pattern`() {
        val notification = createTestNotification(
            title = "Important notification"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "^Important$",
            useRegex = true,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches returns false when regex pattern is invalid`() {
        val notification = createTestNotification()
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "[invalid(regex",
            useRegex = true,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `invalid regex does not affect simple text matching`() {
        val notification = createTestNotification(title = "Test [invalid(regex")
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "[invalid",
            useRegex = false,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns true when priority is within range`() {
        val notification = createTestNotification(priority = 0)
        val rule = TriggerRule(
            webhookId = 1,
            minPriority = -1,
            maxPriority = 1,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns false when priority is below minimum`() {
        val notification = createTestNotification(priority = -2)
        val rule = TriggerRule(
            webhookId = 1,
            minPriority = -1,
            maxPriority = 1,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches returns false when priority is above maximum`() {
        val notification = createTestNotification(priority = 2)
        val rule = TriggerRule(
            webhookId = 1,
            minPriority = -1,
            maxPriority = 1,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches returns true when all conditions match with regex`() {
        val notification = createTestNotification(
            packageName = "com.example.app",
            title = "Urgent alert",
            priority = 1
        )
        val rule = TriggerRule(
            webhookId = 1,
            packageNamePattern = "example",
            contentPattern = ".*alert.*",
            useRegex = true,
            minPriority = 0,
            maxPriority = 2,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns true when all conditions match with simple text`() {
        val notification = createTestNotification(
            packageName = "com.example.app",
            title = "Urgent alert",
            priority = 1
        )
        val rule = TriggerRule(
            webhookId = 1,
            packageNamePattern = "example",
            contentPattern = "alert",
            useRegex = false,
            minPriority = 0,
            maxPriority = 2,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches returns true when null patterns match all`() {
        val notification = createTestNotification()
        val rule = TriggerRule(
            webhookId = 1,
            packageNamePattern = null,
            contentPattern = null,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches list returns false when rules list is empty`() {
        val notification = createTestNotification()
        val rules = emptyList<TriggerRule>()

        val result = triggerMatcher.matches(notification, rules)

        assertThat(result).isFalse()
    }

    @Test
    fun `matches list returns true when any rule matches`() {
        val notification = createTestNotification(packageName = "com.example.app")
        val rules = listOf(
            TriggerRule(webhookId = 1, packageNamePattern = "com.other", enabled = true),
            TriggerRule(webhookId = 2, packageNamePattern = "com.example", enabled = true),
            TriggerRule(webhookId = 3, packageNamePattern = "com.another", enabled = true)
        )

        val result = triggerMatcher.matches(notification, rules)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches searches in bigText when present with regex`() {
        val notification = createTestNotification(
            title = "Title",
            text = "Short text",
            bigText = "This contains the urgent keyword"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = ".*urgent.*",
            useRegex = true,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches searches in bigText when present with simple text`() {
        val notification = createTestNotification(
            title = "Title",
            text = "Short text",
            bigText = "This contains the urgent keyword"
        )
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "urgent",
            useRegex = false,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches is case insensitive for regex pattern`() {
        val notification = createTestNotification(title = "URGENT MESSAGE")
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = ".*urgent.*",
            useRegex = true,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    @Test
    fun `matches is case insensitive for simple text pattern`() {
        val notification = createTestNotification(title = "URGENT MESSAGE")
        val rule = TriggerRule(
            webhookId = 1,
            contentPattern = "urgent",
            useRegex = false,
            enabled = true
        )

        val result = triggerMatcher.matches(notification, rule)

        assertThat(result).isTrue()
    }

    private fun createTestNotification(
        packageName: String = "com.test.app",
        appName: String = "Test App",
        title: String? = "Test Title",
        text: String? = "Test Text",
        subText: String? = null,
        bigText: String? = null,
        priority: Int = 0,
        timestamp: Long = System.currentTimeMillis(),
        iconBase64: String? = null,
        key: String = "test_key"
    ) = NotificationData(
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        subText = subText,
        bigText = bigText,
        priority = priority,
        timestamp = timestamp,
        iconBase64 = iconBase64,
        key = key
    )
}

