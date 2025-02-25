package com.spmadrid.vrepo.data.repositories

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
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : AuthenticationRepository {
    override suspend fun signInWithLark(code: String): AuthenticateResponse? {
        val response = client.get {
            url {
                appendPathSegments("api", "v4", "lark", "user")
            }
            parameter("code", code)
        }

        if (response.status.isSuccess() && response.status.value == 200) {
            return response.body<AuthenticateResponse>()
        }
        return null
    }

    override suspend fun signIn(username: String, password: String): AuthenticateResponse {
        TODO("Not yet implemented")
    }
}