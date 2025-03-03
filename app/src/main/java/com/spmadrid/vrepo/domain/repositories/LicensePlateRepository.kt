package com.spmadrid.vrepo.domain.repositories

import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.ManualNotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatResponse
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.dtos.PlateStatus

interface LicensePlateRepository {
    suspend fun getStatus(plateDetails: PlateCheckInput): PlateStatus
    suspend fun getClientDetails(plateDetails: PlateCheckInput): ClientDetailsResponse?
    suspend fun sendAlertToGroupChat(notifyGroupChatRequest: NotifyGroupChatRequest): NotifyGroupChatResponse?
    suspend fun sendManualAlertToGroupChat(manualNotifyGroupChatRequest: ManualNotifyGroupChatRequest): NotifyGroupChatResponse?
}