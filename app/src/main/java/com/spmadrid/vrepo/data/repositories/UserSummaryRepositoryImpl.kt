package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.domain.dtos.UserSummary
import com.spmadrid.vrepo.domain.repositories.UserSummaryRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.http.appendPathSegments
import javax.inject.Inject

class UserSummaryRepositoryImpl @Inject constructor(
    private val ktorClientProvider: KtorClientProvider
) : UserSummaryRepository {
    override suspend fun getUserSummary(): UserSummary? {
        try {
            val response = ktorClientProvider.client.value.get {
                url {
                    appendPathSegments("api", "v4", "user", "summary")
                }
                timeout {
                    requestTimeoutMillis = 80_000
                }
            }
            Log.d("UserSummaryRepository", "Response: $response")
            val summary = response.body<UserSummary>()
            return summary
        } catch (err: Exception) {
            Log.e("UserSummaryRepository", err.message.toString())
            return null
        }
    }
}