package com.spmadrid.vrepo.domain.services

import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import javax.inject.Inject

class LicensePlateMatchingService @Inject constructor(
    private val licensePlateRepository: LicensePlateRepository
){
    suspend fun getPlateDetails(plateDetails: PlateCheckInput) = licensePlateRepository.getPlateDetails(plateDetails)
}