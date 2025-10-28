package com.notificationforwarder.webhook

import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.model.NotificationData
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class TriggerMatcher {

    fun matches(notification: NotificationData, rule: TriggerRule): Boolean {
        if (!rule.enabled) {
            return false
        }

        // Check package name pattern (always simple text match)
        if (rule.packageNamePattern != null && rule.packageNamePattern.isNotBlank()) {
            if (!notification.packageName.contains(rule.packageNamePattern, ignoreCase = true)) {
                return false
            }
        }

        // Check content pattern (regex or simple text match)
        if (rule.contentPattern != null && rule.contentPattern.isNotBlank()) {
            val content = buildString {
                notification.title?.let { append(it).append(" ") }
                notification.text?.let { append(it).append(" ") }
                notification.subText?.let { append(it).append(" ") }
                notification.bigText?.let { append(it) }
            }

            val matchFound = if (rule.useRegex) {
                // Regex matching
                try {
                    val pattern = Pattern.compile(rule.contentPattern, Pattern.CASE_INSENSITIVE)
                    pattern.matcher(content).find()
                } catch (e: PatternSyntaxException) {
                    // Invalid regex, skip this rule
                    return false
                }
            } else {
                // Simple text matching (case-insensitive contains)
                content.contains(rule.contentPattern, ignoreCase = true)
            }

            if (!matchFound) {
                return false
            }
        }

        // Check priority range
        if (notification.priority < rule.minPriority || notification.priority > rule.maxPriority) {
            return false
        }

        return true
    }

    fun matches(notification: NotificationData, rules: List<TriggerRule>): Boolean {
        // If no rules, don't match (require explicit rules)
        if (rules.isEmpty()) {
            return false
        }

        // Match if ANY rule matches
        return rules.any { matches(notification, it) }
    }
}

