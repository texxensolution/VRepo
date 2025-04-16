package com.spmadrid.vrepo.exts

import android.util.Log
import com.spmadrid.vrepo.domain.dtos.BoundingBox

fun BoundingBox.scaleBoundingBox(
    previewWidth: Int,
    previewHeight: Int,
    imageWidth: Int,
    imageHeight: Int
): BoundingBox {
    val scaleX = previewWidth.toFloat() / imageWidth
    val scaleY = previewHeight.toFloat() / imageHeight

    // Scale the normalized coordinates to pixel coordinates based on preview dimensions
    return this.copy(
        x1 = (this.x1 * previewWidth).coerceIn(0f, previewWidth.toFloat()),
        y1 = (this.y1 * previewHeight).coerceIn(0f, previewHeight.toFloat()),
        x2 = (this.x2 * previewWidth).coerceIn(0f, previewWidth.toFloat()),
        y2 = (this.y2 * previewHeight).coerceIn(0f, previewHeight.toFloat()),
        cx = (this.cx * previewWidth).coerceIn(0f, previewWidth.toFloat()),
        cy = (this.cy * previewHeight).coerceIn(0f, previewHeight.toFloat()),
        w = (this.w * previewWidth).coerceIn(0f, previewWidth.toFloat()),
        h = (this.h * previewHeight).coerceIn(0f, previewHeight.toFloat())
    )
}