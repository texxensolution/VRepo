package com.spmadrid.vrepo.domain.dtos

import android.graphics.Bitmap
import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

data class NotificationEvent(
    val plate: String,
    val priority: String
)
