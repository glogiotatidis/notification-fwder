package com.notificationforwarder.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig
import com.notificationforwarder.data.model.NotificationData
import com.notificationforwarder.data.repository.WebhookRepository
import com.notificationforwarder.service.NotificationForwarderService
import com.notificationforwarder.webhook.TriggerMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebhookEditorViewModel @Inject constructor(
    private val webhookRepository: WebhookRepository,
    private val triggerMatcher: TriggerMatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val webhookId: Long? = savedStateHandle.get<String>("webhookId")?.toLongOrNull()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _headers = MutableStateFlow<Map<String, String>>(emptyMap())
    val headers: StateFlow<Map<String, String>> = _headers.asStateFlow()

    private val _triggerRule = MutableStateFlow(TriggerRule(webhookId = 0))
    val triggerRule: StateFlow<TriggerRule> = _triggerRule.asStateFlow()

    private val _activeNotifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val activeNotifications: StateFlow<List<NotificationData>> = _activeNotifications.asStateFlow()

    val matchingNotifications: StateFlow<List<NotificationData>> = combine(
        _activeNotifications,
        _triggerRule
    ) { notifications, rule ->
        notifications.filter { triggerMatcher.matches(it, rule) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        webhookId?.let { loadWebhook(it) }
        refreshActiveNotifications()
    }

    private fun loadWebhook(id: Long) {
        viewModelScope.launch {
            webhookRepository.getWebhookById(id)?.let { webhook ->
                _url.value = webhook.url
                _headers.value = webhook.headers

                // Load trigger rules
                val rules = webhookRepository.getTriggerRulesSync(id)
                if (rules.isNotEmpty()) {
                    _triggerRule.value = rules.first()
                }
            }
        }
    }

    fun updateUrl(newUrl: String) {
        _url.value = newUrl
    }

    fun addHeader(name: String, value: String) {
        _headers.value = _headers.value + (name to value)
    }

    fun removeHeader(name: String) {
        _headers.value = _headers.value - name
    }

    fun updateTriggerRule(rule: TriggerRule) {
        _triggerRule.value = rule
    }

    fun refreshActiveNotifications() {
        _activeNotifications.value = NotificationForwarderService.getActiveNotifications()
    }

    fun saveWebhook(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val webhook = WebhookConfig(
                    id = webhookId ?: 0,
                    url = _url.value,
                    headers = _headers.value,
                    enabled = true
                )

                val savedWebhookId = if (webhookId != null) {
                    webhookRepository.updateWebhook(webhook)
                    webhookId
                } else {
                    webhookRepository.insertWebhook(webhook)
                }

                // Save or update trigger rule
                val rule = _triggerRule.value.copy(webhookId = savedWebhookId)
                if (webhookId != null) {
                    // Update existing rule or create new one
                    val existingRules = webhookRepository.getTriggerRulesSync(webhookId)
                    if (existingRules.isNotEmpty()) {
                        webhookRepository.updateTriggerRule(rule.copy(id = existingRules.first().id))
                    } else {
                        webhookRepository.insertTriggerRule(rule)
                    }
                } else {
                    webhookRepository.insertTriggerRule(rule)
                }

                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun isValidUrl(): Boolean {
        return android.util.Patterns.WEB_URL.matcher(_url.value).matches() &&
                (_url.value.startsWith("http://") || _url.value.startsWith("https://"))
    }
}

