package com.spmadrid.vrepo.domain.services

import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import javax.inject.Inject

class LicensePlateMatchingService @Inject constructor(
    private val licensePlateRepository: LicensePlateRepository
){
    suspend fun isPositive(plateDetails: PlateCheckInput) = licensePlateRepository.isPositive(plateDetails)
    suspend fun sendAlertToGroupChat(notifyGroupChatRequest: NotifyGroupChatRequest) = licensePlateRepository.sendAlertToGroupChat(notifyGroupChatRequest)
}