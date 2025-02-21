package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import javax.inject.Inject


class LicensePlateRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : LicensePlateRepository{
    override suspend fun getPlateDetails(plateDetails: PlateCheckInput): ClientDetailsResponse? {
        val response = client.post {
            url {
                appendPathSegments("api", "v4", "plate", "check")
            }
            contentType(ContentType.Application.Json)
            setBody(plateDetails)
        }
        Log.d(TAG, response.toString())

        if (response.status == HttpStatusCode.Forbidden) {
            return null
        }
        val body = response.body<ClientDetailsResponse>()
        Log.d(TAG, body.toString())

        return body
    }

    companion object {
        const val TAG = "LicensePlateRepositoryImpl"
    }
}