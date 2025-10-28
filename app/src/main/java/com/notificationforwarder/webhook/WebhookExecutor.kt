package com.notificationforwarder.webhook

import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.notificationforwarder.api.WebhookApi
import com.notificationforwarder.data.entity.NotificationLog
import com.notificationforwarder.data.model.NotificationData
import com.notificationforwarder.data.repository.NotificationLogRepository
import com.notificationforwarder.data.repository.WebhookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebhookExecutor @Inject constructor(
    private val webhookApi: WebhookApi,
    private val webhookRepository: WebhookRepository,
    private val notificationLogRepository: NotificationLogRepository,
    private val triggerMatcher: TriggerMatcher,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "WebhookExecutor"
        private const val MAX_RETRIES = 3
    }

    suspend fun processNotification(notification: NotificationData) {
        withContext(Dispatchers.IO) {
            try {
                // Get all enabled webhooks
                val webhooks = webhookRepository.getEnabledWebhooks()
                
                webhooks.collect { webhookList ->
                    webhookList.forEach { webhook ->
                        // Get trigger rules for this webhook
                        val triggerRules = webhookRepository.getTriggerRulesSync(webhook.id)
                        
                        // Check if notification matches any trigger rule
                        if (triggerMatcher.matches(notification, triggerRules)) {
                            Log.d(TAG, "Notification matches webhook ${webhook.id}, sending...")
                            sendWebhook(webhook.id, webhook.url, webhook.headers, notification)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification: ${e.message}", e)
            }
        }
    }

    private suspend fun sendWebhook(
        webhookId: Long,
        url: String,
        headers: Map<String, String>,
        notification: NotificationData
    ) {
        var lastError: String? = null
        var lastStatusCode: Int? = null
        var responseBodyText: String? = null

        // Build webhook payload
        val payload = WebhookPayload(
            packageName = notification.packageName,
            appName = notification.appName,
            title = notification.title,
            text = notification.text,
            subText = notification.subText,
            bigText = notification.bigText,
            priority = notification.priority,
            timestamp = notification.timestamp,
            iconBase64 = notification.iconBase64,
            device = DeviceInfo(
                androidVersion = Build.VERSION.RELEASE,
                deviceModel = Build.MODEL,
                deviceManufacturer = Build.MANUFACTURER
            )
        )

        val jsonPayload = gson.toJson(payload)
        Log.d(TAG, "Sending webhook to $url with payload: ${jsonPayload.take(200)}...")

        // Retry logic
        var attempt = 0
        var success = false

        while (attempt < MAX_RETRIES && !success) {
            attempt++
            
            try {
                val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
                val response = webhookApi.sendWebhook(url, headers, requestBody)
                
                lastStatusCode = response.code()
                responseBodyText = response.body()?.string()?.take(500)

                success = response.isSuccessful
                
                if (success) {
                    Log.d(TAG, "Webhook sent successfully to $url (status: $lastStatusCode)")
                } else {
                    lastError = "HTTP ${response.code()}: ${response.message()}"
                    Log.w(TAG, "Webhook failed: $lastError")
                    
                    // Don't retry on client errors (4xx)
                    if (response.code() in 400..499) {
                        break
                    }
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown error"
                Log.e(TAG, "Webhook error (attempt $attempt/$MAX_RETRIES): $lastError", e)
                
                // Wait before retry
                if (attempt < MAX_RETRIES) {
                    kotlinx.coroutines.delay(1000L * attempt)
                }
            }
        }

        // Log the result
        val log = NotificationLog(
            webhookId = webhookId,
            webhookUrl = url,
            packageName = notification.packageName,
            appName = notification.appName,
            title = notification.title,
            text = notification.text,
            priority = notification.priority,
            timestamp = notification.timestamp,
            iconBase64 = notification.iconBase64,
            httpStatusCode = lastStatusCode,
            success = success,
            errorMessage = if (!success) lastError else null,
            responseBody = responseBodyText
        )

        notificationLogRepository.insertLog(log)
    }
}

