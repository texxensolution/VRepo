package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spmadrid.vrepo.data.repositories.WebSocketRepository
import com.spmadrid.vrepo.domain.dtos.CurrentDeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject


@HiltViewModel
class DeviceTrackingViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initWebSocket() {
        scope.launch {
            webSocketRepository.connect()
        }
    }

    fun sendCurrentDeviceInfo(currentDeviceInfo: CurrentDeviceInfo) {
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