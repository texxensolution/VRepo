package com.spmadrid.vrepo.data.providers

import android.util.Log
import com.spmadrid.vrepo.domain.services.TokenManagerService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorClientProvider @Inject constructor(
    private val tokenManagerService: TokenManagerService
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _client = MutableStateFlow(createHttpClient(null))
    val client: StateFlow<HttpClient> get() = _client

    init {
        scope.launch {
            tokenManagerService.tokenFlow.collectLatest { token ->
                _client.value = createHttpClient(token)
                Log.d(TAG, "Initialized HTTP Client: reloaded with new = $token")
            }
        }
    }

    private fun createHttpClient(token: String?): HttpClient {
        return HttpClient(CIO) {
            defaultRequest {
                host = "58.97.187.251"
                port = 8000
                url {
                    protocol = URLProtocol.HTTP
                }
            }
            install(WebSockets) {
                pingIntervalMillis = PING_WEBSOCKET_INTERVAL
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        token?.let { BearerTokens(it, "") }
                    }
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    companion object {
        const val TAG = "KtorClientProvider"
        const val PING_WEBSOCKET_INTERVAL = 80L
    }
}