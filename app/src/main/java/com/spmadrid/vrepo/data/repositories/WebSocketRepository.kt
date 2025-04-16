package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.constants.Constants
import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.domain.dtos.CurrentDeviceLocation
import com.spmadrid.vrepo.domain.services.LocationManagerService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class WebSocketRepository @Inject constructor(
    private val ktorClientProvider: KtorClientProvider,
    private val locationManagerService: LocationManagerService,
    private val tokenManagerService: TokenManagerService
) {
    private var webSocketSession: DefaultWebSocketSession? = null
    private var shouldReconnect = true

    private val _incomingMessages = MutableSharedFlow<String>(replay = 1)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        CoroutineScope(Dispatchers.IO).launch {
            tokenManagerService.tokenFlow.collect { token ->
                shouldReconnect = !token.isNullOrEmpty() // Set shouldReconnect based on token presence
                Log.d(TAG, "Token state updated: $shouldReconnect")
            }
        }
    }


    suspend fun connect() {
        if (_isConnected.value) return

        while (shouldReconnect) {
            if (tokenManagerService.isTokenExpired()) {
                tokenManagerService.clearToken()
                webSocketSession?.close()
                shouldReconnect = false
            }
            try {
                ktorClientProvider.client.value.webSocket(
                    method = HttpMethod.Get,
                    host = Constants.SERVER_URL,
                    path = "/ws/location",
                    port = Constants.SERVER_PORT
                ) {
                    webSocketSession = this
                    _isConnected.value = true

                    val currentLocation = locationManagerService.getCurrentLocation()

                    val initialCurrentDeviceInfo = CurrentDeviceLocation(
                        latitude = currentLocation?.latitude ?: 0.0,
                        longitude = currentLocation?.longitude ?: 0.0
                    )

                    val initialData = Json.encodeToString(initialCurrentDeviceInfo)

                    send(Frame.Text(initialData))

                    for (message in incoming) {
                        when (message) {
                            is Frame.Text -> {
                                val currentMessage = message.readText()

                                val data: JsonElement = Json.decodeFromString(currentMessage)
                                println("WSData: ${data.jsonObject["status"]}")
                                val statusElement = data.jsonObject["status"]
                                val status = statusElement?.jsonPrimitive?.content

                                if (status == "FORCED_LOGOUT") {
                                    Log.d("FORCED_LOGOUT_EVENT", "Clearing token and closing websocket connection!")
                                    tokenManagerService.clearToken()
                                    webSocketSession?.close()
                                    shouldReconnect = false
                                }
                            }
                            else -> Log.d(TAG, "Received non-text frame!")
                        }
                    }
                }
            } catch (e: Exception) {
                _isConnected.value = false
                webSocketSession?.close()
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