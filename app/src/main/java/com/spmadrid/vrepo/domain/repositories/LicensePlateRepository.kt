package com.spmadrid.vrepo.domain.repositories

import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatResponse
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput

interface LicensePlateRepository {
    suspend fun isPositive(plateDetails: PlateCheckInput): Boolean
    suspend fun sendAlertToGroupChat(notifyGroupChatRequest: NotifyGroupChatRequest): NotifyGroupChatResponse?
}