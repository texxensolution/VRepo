package com.spmadrid.vrepo.domain.repositories

import android.location.Location
import com.spmadrid.vrepo.domain.dtos.Coordinate

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
}