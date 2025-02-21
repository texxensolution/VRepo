package com.spmadrid.vrepo.domain.repositories

import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput

interface LicensePlateRepository {
    suspend fun getPlateDetails(plateDetails: PlateCheckInput): ClientDetailsResponse?
}