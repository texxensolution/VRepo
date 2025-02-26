package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatResponse
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.InternalAPI
import java.util.UUID
import javax.inject.Inject


class LicensePlateRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : LicensePlateRepository {
    override suspend fun isPositive(plateDetails: PlateCheckInput): Boolean {
        val response = client.post {
            url {
                appendPathSegments("api", "v4", "plate", "check")
            }
            contentType(ContentType.Application.Json)
            setBody(plateDetails)
        }
        if (response.status == HttpStatusCode.Forbidden && !response.status.isSuccess()) {
            return false
        }
        val body = response.body<ClientDetailsResponse>()
        Log.d(TAG, "isPositive: $body")
        return body.status == "POSITIVE"
    }

    @OptIn(InternalAPI::class)
    override suspend fun sendAlertToGroupChat(notifyGroupChatRequest: NotifyGroupChatRequest): NotifyGroupChatResponse? {
        val uuid = UUID.randomUUID().toString()
        val response = client.post {
            url {
                appendPathSegments("api", "v4", "notify", "group-chat")
            }
            contentType(ContentType.MultiPart.FormData)
            body = MultiPartFormDataContent(
                formData {
                    append("image", notifyGroupChatRequest.image, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"$uuid.jpg\"")
                    })

                    append("plate", notifyGroupChatRequest.plate)
                    append("detection_type", notifyGroupChatRequest.detectionType)
                    append("latitude", notifyGroupChatRequest.latitude)
                    append("longitude", notifyGroupChatRequest.longitude)
                }
            )
        }
        if (response.status == HttpStatusCode.OK && response.status.isSuccess()) {
            val body = response.body<NotifyGroupChatResponse>()
            return body
        }
        return null
    }

    companion object {
        const val TAG = "LicensePlateRepositoryImpl"
    }
}