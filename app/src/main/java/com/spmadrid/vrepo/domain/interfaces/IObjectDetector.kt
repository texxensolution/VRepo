package com.spmadrid.vrepo.domain.interfaces

import android.graphics.Bitmap
import com.spmadrid.vrepo.domain.dtos.BoundingBox

interface IObjectDetector {
    suspend fun detect(frame: Bitmap): List<BoundingBox>?
    fun close()
    fun initialize()
}