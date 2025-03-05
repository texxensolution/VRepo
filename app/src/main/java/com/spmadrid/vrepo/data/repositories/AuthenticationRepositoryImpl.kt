package com.spmadrid.vrepo.data.repositories

import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.domain.dtos.AuthenticateResponse
import com.spmadrid.vrepo.domain.repositories.AuthenticationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val ktorClientProvider: KtorClientProvider
) : AuthenticationRepository {
    override suspend fun signInWithLark(code: String): AuthenticateResponse? {
        val response = ktorClientProvider.client.value.get {
            url {
                appendPathSegments("api", "v4", "lark", "user")
            }
            parameter("code", code)
        }

        if (response?.status?.isSuccess() == true && response?.status?.value == 200) {
            return response.body<AuthenticateResponse>()
        }

        return null
    }

    override suspend fun signInWithUserAndPassword(
        username: String,
        password: String
    ): AuthenticateResponse? {
        val response = ktorClientProvider.client.value.post {
            url {
                appendPathSegments("api", "v4", "auth")
            }
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }

        if (response.status.isSuccess() && response.status.value == 200) {
            return response.body<AuthenticateResponse>()
        }

        return null
    }
}