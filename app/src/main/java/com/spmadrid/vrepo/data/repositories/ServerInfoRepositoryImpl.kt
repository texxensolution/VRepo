package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.domain.repositories.ServerInfoRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class ServerInfoRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : ServerInfoRepository {
    override suspend fun isServerRunning(): Boolean {
        try {
            val response = client.get("/")

            if (response.status == HttpStatusCode.OK) {
                return true
            }
        } catch (exc: Exception) {
            Log.d(TAG, exc.message.toString())
        }
        return false
    }

    companion object {
        const val TAG = "ServerInfoRepositoryImpl"
    }
}