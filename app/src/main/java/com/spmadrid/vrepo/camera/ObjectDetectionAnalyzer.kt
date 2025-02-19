package com.spmadrid.vrepo.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spmadrid.vrepo.domain.dtos.BoundingBox
import com.spmadrid.vrepo.domain.repositories.IObjectDetector
import com.spmadrid.vrepo.exts.crop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ObjectDetectionAnalyzer @Inject constructor(
    private val objectDetector: IObjectDetector,
    private val onDetectedText: (String) -> Unit,
    private val onNotifyApp: (String) -> Unit
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
                            if (visionText.text.isNotBlank()) {
                                onDetectedText(visionText.text)
                                val plateCheck = mockPlateCheck(visionText.text)

                                if (plateCheck.status == "POSITIVE") {
                                    onNotifyApp("${plateCheck.status}: ${plateCheck.plate}")
                                }
                            }
                            continuation.resume(visionText)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error processing image", e)
//                            continuation.resumeWithException(e)
                            continuation.resume(null)
                        }
                }

                Log.d(TAG, "Vision Text Recognition: ${visionText?.text}")
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
data class PlateCheck(
    val status: String,
    val plate: String
)
fun mockPlateCheck(text: String): PlateCheck {
    val plates = listOf("NBC 1234", "CAX 3200", "DBA 4658")
    if (plates.contains(text)) {
        return PlateCheck("POSITIVE", text)
    }
    return PlateCheck("NEGATIVE", text)
}