package com.spmadrid.vrepo.domain.repositories

import android.location.Location
import com.spmadrid.vrepo.domain.dtos.Coordinate
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeLocationUpdates(): Flow<Location>
    suspend fun getCurrentLocation(): Location?
}