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

    private val _triggerRules = MutableStateFlow<List<TriggerRule>>(emptyList())
    val triggerRules: StateFlow<List<TriggerRule>> = _triggerRules.asStateFlow()

    private val _currentRule = MutableStateFlow<TriggerRule?>(null)
    val currentRule: StateFlow<TriggerRule?> = _currentRule.asStateFlow()

    private val _activeNotifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val activeNotifications: StateFlow<List<NotificationData>> = _activeNotifications.asStateFlow()

    val matchingNotifications: StateFlow<List<NotificationData>> = combine(
        _activeNotifications,
        _triggerRules
    ) { notifications, rules ->
        if (rules.isEmpty()) {
            emptyList()
        } else {
            notifications.filter { notification ->
                triggerMatcher.matches(notification, rules)
            }
        }
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
                _triggerRules.value = rules
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

    fun addTriggerRule() {
        _currentRule.value = TriggerRule(webhookId = webhookId ?: 0)
    }

    fun updateCurrentRule(rule: TriggerRule) {
        _currentRule.value = rule
    }

    fun saveCurrentRule() {
        viewModelScope.launch {
            _currentRule.value?.let { rule ->
                if (rule.id == 0L) {
                    // New rule
                    webhookRepository.insertTriggerRule(rule.copy(webhookId = webhookId ?: 0))
                } else {
                    // Update existing
                    webhookRepository.updateTriggerRule(rule)
                }
                // Reload rules
                webhookId?.let { id ->
                    _triggerRules.value = webhookRepository.getTriggerRulesSync(id)
                }
                _currentRule.value = null
            }
        }
    }

    fun cancelEditRule() {
        _currentRule.value = null
    }

    fun toggleTriggerRule(rule: TriggerRule) {
        viewModelScope.launch {
            webhookRepository.updateTriggerRule(rule.copy(enabled = !rule.enabled))
            webhookId?.let { id ->
                _triggerRules.value = webhookRepository.getTriggerRulesSync(id)
            }
        }
    }

    fun deleteTriggerRule(rule: TriggerRule) {
        viewModelScope.launch {
            webhookRepository.deleteTriggerRule(rule)
            webhookId?.let { id ->
                _triggerRules.value = webhookRepository.getTriggerRulesSync(id)
            }
        }
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

