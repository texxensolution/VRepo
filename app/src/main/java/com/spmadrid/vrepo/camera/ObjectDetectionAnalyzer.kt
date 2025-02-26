package com.spmadrid.vrepo.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spmadrid.vrepo.domain.dtos.DetectedTextResult
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.exts.crop
import com.spmadrid.vrepo.exts.removeDiacritics
import com.spmadrid.vrepo.exts.removeSpecialCharacters
import com.spmadrid.vrepo.exts.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ObjectDetectionAnalyzer @Inject constructor(
    private val objectDetector: IObjectDetector,
    private val onDetectedText: (DetectedTextResult) -> Unit,
) : ImageAnalysis.Analyzer {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            imageProxy.image ?: return@launch imageProxy.close()

            val bitmap = imageProxy.toBitmap()
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            // Call detector repository for detection
            val boxes = objectDetector.detect(rotatedBitmap)
            Log.d(TAG, "boxes: $boxes")

            if (boxes != null) {
                val box = boxes[0]
                val cropped = rotatedBitmap.crop(box)

                val visionText = suspendCoroutine { continuation ->
                    textRecognizer.process(cropped, 0)
                        .addOnSuccessListener { visionText ->
                            Log.d(TAG, "Vision Text Recognized: ${visionText?.text}")
                            continuation.resume(visionText)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error processing image", e)
                            continuation.resume(null)
                        }
                }

                if (visionText != null) {
                    val normalized = visionText.text
                        .removeDiacritics()
                        .removeSpecialCharacters()
                        .uppercase(Locale.ROOT)

                    if (normalized.isBlank()) return@launch

                    onDetectedText(
                        DetectedTextResult(
                            normalized,
                            box.clsName,
                            rotatedBitmap.toByteArray()
                        )
                    )
                }
            }
            // Throttling: Wait for a specified period before processing the next image
            delay(THROTTLE_TIMEOUT_MS)
        }.invokeOnCompletion { exception ->
            exception?.printStackTrace()
            imageProxy.close()
        }
    }
    companion object {
        const val THROTTLE_TIMEOUT_MS = 1_000L
        const val TAG = "ObjectDetectionAnalyzer"
    }
}