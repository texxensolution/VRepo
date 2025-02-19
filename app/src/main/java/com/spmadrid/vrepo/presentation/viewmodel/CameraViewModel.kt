package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel(){
    private val _detectedText: MutableStateFlow<String> = MutableStateFlow("")
    val detectedText: StateFlow<String> = _detectedText

    fun updateDetectedText(text: String) {
        viewModelScope.launch {
            _detectedText.value = text
        }
    }

    private val _notification: MutableStateFlow<NotificationEvent?> = MutableStateFlow(null)
    val notification: StateFlow<NotificationEvent?> = _notification

    fun updateNotification(notificationEvent: NotificationEvent) {
        viewModelScope.launch {
            _notification.value = notificationEvent
        }
    }

    private val _showNotification: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showNotification: StateFlow<Boolean> = _showNotification

    fun notifyApp(notificationEvent: NotificationEvent, timeDelay: Long = 5_000L) {
        viewModelScope.launch {
            _showNotification.value = true
            updateNotification(notificationEvent)
            delay(timeDelay)
            _showNotification.value = false
        }
    }
}