package com.notificationforwarder.ui.viewmodel

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationforwarder.data.repository.NotificationLogRepository
import com.notificationforwarder.data.repository.WebhookRepository
import com.notificationforwarder.service.NotificationForwarderService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    webhookRepository: WebhookRepository,
    notificationLogRepository: NotificationLogRepository
) : ViewModel() {

    val webhookCount = webhookRepository.getWebhookCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificationCount = notificationLogRepository.getLogCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun isNotificationServiceEnabled(): Boolean {
        val enabledListeners = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val componentName = ComponentName(context, NotificationForwarderService::class.java)
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }
}

