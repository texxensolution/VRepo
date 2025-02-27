package com.spmadrid.vrepo.exts

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.spmadrid.vrepo.domain.dtos.BoundingBox
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

val TAG = "Bitmap"
const val PADDING = 10
fun Bitmap.crop(boundingBox: BoundingBox): Bitmap {
    // Get the cropping coordinates from the scaleBoundingBox function
    Log.d(TAG, "Cropped using provided bounding box!")
    val (topLeft, bottomRight) = this.scaleBoundingBox(boundingBox)
    val left = topLeft.first
    val top = topLeft.second
    val right = bottomRight.first
    val bottom = bottomRight.second
    // Crop the bitmap using the calculated coordinates
    return Bitmap.createBitmap(
        this,
        left,
        top,
        right - left,
        bottom - top
    )
}

fun Bitmap.scaleBoundingBox(
    boundingBox: BoundingBox,
): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    val left = (boundingBox.x1 * this.width).toInt().coerceIn(0, this.width - 1)
    val top = (boundingBox.y1 * this.height).toInt().coerceIn(0, this.height - 1)
    val right = (boundingBox.x2 * this.width).toInt().coerceIn(left + 1, this.width) // Ensure width > 0
    val bottom = (boundingBox.y2 * this.height).toInt().coerceIn(top + 1, this.height) // Ensure height > 0
    return Pair(Pair(left, top), Pair(right, bottom))
}

fun Bitmap.saveToStorage(fileName: String = UUID.randomUUID().toString()): Boolean {
    return try {
        // Get the directory to save the image (e.g., Pictures folder)
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyApp"
        )

        // Make sure the directory exists, if not, create it
        if (!directory.exists()) {
            directory.mkdirs()
        }
        // Create a file inside that directory with the provided file name
        val file = File(
            directory,
            "$fileName.jpg"
        )

        // Open a FileOutputStream to write the bitmap to the file
        FileOutputStream(file).use { fos ->
            // Compress the bitmap into a JPG format and write it to the FileOutputStream
            this.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        Log.d(TAG, "Bitmap saved to: ${file.absolutePath}")
        true // Successfully saved
    } catch (e: IOException) {
        Log.e(TAG, "Error saving bitmap", e)
        false // Failed to save
    }
}

fun Bitmap.toByteArray(): ByteArray {
    val outputStream = java.io.ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return outputStream.toByteArray()
}
