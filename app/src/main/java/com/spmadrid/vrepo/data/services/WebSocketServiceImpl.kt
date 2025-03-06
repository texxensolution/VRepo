package com.spmadrid.vrepo.data.services

import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.domain.services.WebSocketService
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import javax.inject.Inject

class WebSocketServiceImpl @Inject constructor(
    private val ktorClientProvider: KtorClientProvider
) : WebSocketService{

    override suspend fun connect() {
//        ktorClientProvider.client.value.webSocket {
//
//        }
    }

    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

}