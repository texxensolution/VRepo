package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spmadrid.vrepo.domain.dtos.NotificationMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject


@HiltViewModel
class RealtimeNotificationViewModel @Inject constructor(): ViewModel() {
    private val _incomingMessage = MutableSharedFlow<NotificationMessage>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val incomingMessage = _incomingMessage.asSharedFlow()

    fun sendNotification(title: String, message: String) {
        val messageObj = NotificationMessage(title, message)
        _incomingMessage.tryEmit(messageObj)
    }
}