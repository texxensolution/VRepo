package com.spmadrid.vrepo.data.repositories

import android.util.Log
import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.ManualNotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatResponse
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.dtos.PlateStatus
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
    private val ktorClientProvider: KtorClientProvider
) : LicensePlateRepository {
    override suspend fun getStatus(plateDetails: PlateCheckInput): PlateStatus {
        val response = ktorClientProvider.client.value.post {
            url {
                appendPathSegments("api", "v4", "plate", "check")
            }
            contentType(ContentType.Application.Json)
            setBody(plateDetails)
        }
        if (response.status == HttpStatusCode.Forbidden && !response.status.isSuccess()) {
            return PlateStatus.NEGATIVE
        }

        val body = response.body<ClientDetailsResponse>()
        Log.d(TAG, "isPositive: $body")

        return when (body.status) {
            "POSITIVE" -> PlateStatus.POSITIVE
            "FOR_CONFIRMATION" -> PlateStatus.FOR_CONFIRMATION
            else -> PlateStatus.NEGATIVE
        }
    }

    override suspend fun getClientDetails(plateDetails: PlateCheckInput): ClientDetailsResponse? {
        val response = ktorClientProvider.client.value.post {
            url {
                appendPathSegments("api", "v4", "plate", "check")
            }
            contentType(ContentType.Application.Json)
            setBody(plateDetails)
        }
        if (response.status == HttpStatusCode.Forbidden && !response.status.isSuccess()) {
            return null
        }

        val body = response.body<ClientDetailsResponse>()
        Log.d(TAG, "isPositive: $body")

        return body
    }

    override suspend fun sendManualAlertToGroupChat(manualNotifyGroupChatRequest: ManualNotifyGroupChatRequest): NotifyGroupChatResponse? {
        try {
            println("Manual alert to group chat: $manualNotifyGroupChatRequest")
            val response = ktorClientProvider.client.value.post {
                url {
                    appendPathSegments("api", "v4", "notify", "group-chat", "manual")
                }
                contentType(ContentType.Application.Json)
                setBody(manualNotifyGroupChatRequest)
            }

            if (response.status == HttpStatusCode.OK && response.status.isSuccess()) {
                val body = response.body<NotifyGroupChatResponse>()
                return body
            }
        } catch (err: Exception) {
            Log.e(TAG, "Error: $err")
        }
        return null
    }

    @OptIn(InternalAPI::class)
    override suspend fun sendAlertToGroupChat(notifyGroupChatRequest: NotifyGroupChatRequest): NotifyGroupChatResponse? {
        val uuid = UUID.randomUUID().toString()
        val response = ktorClientProvider.client.value.post {
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