package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.data.providers.KtorClientProvider
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class WebSocketRepository @Inject constructor(
    private val ktorClientProvider: KtorClientProvider
) {
    private var webSocketSession: DefaultWebSocketSession? = null

    private val _incomingMessages = MutableSharedFlow<String>(replay = 1)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected: StateFlow<Boolean> = _isConnected


    suspend fun connect() {
        if (_isConnected.value) return

        while (true) {
            try {
                ktorClientProvider.client.value.webSocket(
                    method = HttpMethod.Get,
                    host = "elephant-humble-herring.ngrok-free.app",
                    path = "/api/v4/ping"
                ) {
                    webSocketSession = this
                    _isConnected.value = true

                    send(Frame.Text("websocket started"))

                    for (message in incoming) {
                        when (message) {
                            is Frame.Text -> {
                                _incomingMessages.emit(message.readText())
                                Log.d(TAG, message.readText())
                            }
                            else -> Log.d(TAG, "Received non-text frame!")
                        }
                    }
                }
            } catch (e: Exception) {
                _isConnected.value = false
                Log.e(TAG, "WebSocket Connection Error: ${e.message}")

                delay(3000L)
            }
        }
    }

    suspend fun sendMessage(message: String) {
        try {
            webSocketSession?.send(Frame.Text(message))
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket Send Error: ${e.message}")
        }
    }

    fun disconnect() {
        webSocketSession?.cancel()
        webSocketSession = null
        _isConnected.value = false
    }

    companion object {
        const val TAG = "WebSocketRepository"
    }
}