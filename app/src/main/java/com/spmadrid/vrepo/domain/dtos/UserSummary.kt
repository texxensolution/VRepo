package com.spmadrid.vrepo.domain.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSummary(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("unique_scanned_count") val uniqueScannedCount: Int,
    @SerialName("positive_count") val positiveCount: Int
)
