package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spmadrid.vrepo.data.repositories.WebSocketRepository
import com.spmadrid.vrepo.domain.dtos.CurrentDeviceLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject


@HiltViewModel
class PersistentSocketViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.IO)
    val isConnected: StateFlow<Boolean> = webSocketRepository.isConnected

    val incomingMessages = webSocketRepository.incomingMessages

    fun startPersistentConnection() {
        scope.launch {
            webSocketRepository.connect()
        }
    }

    fun stopPersistentConnection() {
        scope.launch {
            webSocketRepository.disconnect()
        }
    }

    fun sendCurrentDeviceInfo(currentDeviceInfo: CurrentDeviceLocation) {
        scope.launch {
            val data = Json.encodeToString(currentDeviceInfo)
            webSocketRepository.sendMessage(data)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}