package com.notificationforwarder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationforwarder.data.entity.WebhookConfig
import com.notificationforwarder.data.repository.WebhookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebhookListViewModel @Inject constructor(
    private val webhookRepository: WebhookRepository
) : ViewModel() {

    val webhooks = webhookRepository.getAllWebhooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteWebhook(webhook: WebhookConfig) {
        viewModelScope.launch {
            webhookRepository.deleteWebhook(webhook)
        }
    }

    fun toggleWebhook(webhook: WebhookConfig) {
        viewModelScope.launch {
            webhookRepository.updateWebhook(webhook.copy(enabled = !webhook.enabled))
        }
    }
}

