package com.notificationforwarder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationforwarder.data.entity.NotificationLog
import com.notificationforwarder.data.repository.NotificationLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val notificationLogRepository: NotificationLogRepository
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<Boolean?>(null)
    val filterStatus: StateFlow<Boolean?> = _filterStatus.asStateFlow()

    val logs: StateFlow<List<NotificationLog>> = _filterStatus.flatMapLatest { status ->
        when (status) {
            null -> notificationLogRepository.getAllLogs()
            else -> notificationLogRepository.getLogsByStatus(status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(success: Boolean?) {
        _filterStatus.value = success
    }

    fun clearHistory() {
        viewModelScope.launch {
            notificationLogRepository.clearAllLogs()
        }
    }
}

