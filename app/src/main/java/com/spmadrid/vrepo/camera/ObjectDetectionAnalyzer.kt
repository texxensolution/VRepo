package com.spmadrid.vrepo.camera

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spmadrid.vrepo.domain.dtos.BoundingBox
import com.spmadrid.vrepo.domain.dtos.DetectedTextResult
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.exts.crop
import com.spmadrid.vrepo.exts.drawBoundingBox
import com.spmadrid.vrepo.exts.removeDiacritics
import com.spmadrid.vrepo.exts.removeSpecialCharacters
import com.spmadrid.vrepo.exts.rotate
import com.spmadrid.vrepo.exts.scaleBoundingBox
import com.spmadrid.vrepo.exts.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.max

class ObjectDetectionAnalyzer @Inject constructor(
    private val objectDetector: IObjectDetector,
    private val cameraControl: CameraControl?,
    private val previewView: PreviewView,
    private val onDetectedText: (DetectedTextResult) -> Unit,
    private val isAutoZoomEnabled: Boolean
) : ImageAnalysis.Analyzer {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val executors = Executors.newFixedThreadPool(4)

    private val textRecognitionOptions = TextRecognizerOptions.Builder().setExecutor(executors).build()
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(textRecognitionOptions)

    private var currentZoomRatio = 1.0f  // Track current zoom level\
    private var currentZoom = 1.0f
    private var zoomResetJob: Job? = null  // Track zoom-out coroutine
    private var lastDetectionTime = System.currentTimeMillis()
    private val mainHandler = Handler(Looper.getMainLooper()) // Main thread handler

    private val lastDetectedTexts = mutableMapOf<String, Long>() // Store last detections

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            try {
                val image = imageProxy.image ?: return@launch
                val bitmap = imageProxy.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees)

                // Call object detector
                val boxes = objectDetector.detect(bitmap).orEmpty()
                Log.d("boxes", boxes.toString())

                if (boxes.isNotEmpty()) {
                    lastDetectionTime = System.currentTimeMillis() // ✅ Only reset on valid detection
                    zoomResetJob?.cancel()  // ✅ Cancel scheduled zoom-out since an object is detected
                    Log.d(TAG, "Object detected, zoom-out canceled")

                    val box = boxes.first()

                    if (isAutoZoomEnabled == true) {
                        Log.d("AutoZoomEnabled", isAutoZoomEnabled.toString())
                        adjustZoom(box, bitmap.width, bitmap.height)
                    }
                } else {
                    Log.d(TAG, "No objects detected - Ensuring zoom-out is scheduled")
                    scheduleZoomOut()  // ✅ Ensure zoom-out scheduling happens properly
                    return@launch
                }

                val box = boxes.first()

                val cropped = bitmap.crop(box)

                // Skip small images
                if (cropped.width < 32 || cropped.height < 32) {
                    Log.w(TAG, "Skipping small image: ${cropped.width}x${cropped.height}")
                    return@launch
                }

                val visionText = processTextRecognition(cropped) ?: return@launch
                val normalized = visionText.text
                    .removeDiacritics()
                    .removeSpecialCharacters()
                    .uppercase(Locale.ROOT)

                if (normalized.isBlank()) return@launch

                // Check if the text was recently detected (Throttle)
                val currentTime = System.currentTimeMillis()
                val lastTime = lastDetectedTexts[normalized] ?: 0
                if (currentTime - lastTime > THROTTLE_TIMEOUT_MS) {
                    lastDetectedTexts[normalized] = currentTime
                    onDetectedText(DetectedTextResult(normalized, box.clsName, bitmap.toByteArray()))
                } else {
                    Log.d(TAG, "Throttled text: $normalized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in analyze()", e)
            } finally {
                imageProxy.close()
            }
        }
    }

    private suspend fun processTextRecognition(bitmap: Bitmap): Text? = withContext(Dispatchers.Default) {
        suspendCoroutine { continuation ->
            textRecognizer.process(bitmap, 0)
                .addOnSuccessListener { text -> continuation.resume(text) }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Text recognition error", e)
                    continuation.resume(null)
                }
        }
    }

    private fun adjustZoom(boundingBox: BoundingBox?, imageWidth: Int, imageHeight: Int) {
        val targetZoom = if (boundingBox == null) {
            1.0f // Reset to default zoom
        } else {
            val objectWidthRatio = boundingBox.w / imageWidth.toFloat()
            val objectHeightRatio = boundingBox.h / imageHeight.toFloat()
            val objectSizeRatio =
                max(objectWidthRatio, objectHeightRatio) // Use max to preserve aspect ratio
            val targetObjectSize = 0.6f // Adjust based on desired zoom behavior

            // Compute zoom level and clamp to limits
            (targetObjectSize / objectSizeRatio).coerceIn(1.0f, 4.0f)
        }

        // Smooth out zoom transitions
        if (abs(currentZoom - targetZoom) > 0.1f) {
            animateZoom(currentZoom, targetZoom)
        }
    }

    private fun animateZoom(startZoom: Float, endZoom: Float) {
        if (startZoom == endZoom) return // Skip if no change

        mainHandler.post { // Ensure this runs on the main UI thread
            val animator = ValueAnimator.ofFloat(startZoom, endZoom).apply {
                duration = 300 // Smooth zoom over 300ms
                addUpdateListener { animation ->
                    val zoomValue = animation.animatedValue as Float
                    setZoomSmoothly(zoomValue)
                    currentZoom = zoomValue // Update current zoom
                }
            }
            animator.start()
        }
    }

    /**
     * Applies zoom only if the change is significant (to avoid flickering)
     */
    private fun setZoomSmoothly(targetZoom: Float) {
        if (abs(currentZoomRatio - targetZoom) > 0.05f) {  // Change threshold
            cameraControl?.setZoomRatio(targetZoom)
            currentZoomRatio = targetZoom
        }
    }

    /**
     * Schedules a zoom-out reset after 3 seconds.
     */
    private fun scheduleZoomOut() {
        // If a job is already running, don't reschedule
        if (zoomResetJob?.isActive == true) {
            Log.d(TAG, "⏳ Zoom-out already scheduled, skipping...")
            return
        }

        Log.d(TAG, "🔵 scheduleZoomOut() called. Scheduling zoom-out in 3 seconds...")

        zoomResetJob = scope.launch {
            delay(1000)  // Wait for 3 seconds

            val elapsedTime = System.currentTimeMillis() - lastDetectionTime
            Log.d(TAG, "⏳ Checking elapsed time: $elapsedTime ms since last detection")

            if (elapsedTime >= 1000) {
                Log.d(TAG, "✅ Zooming out to 1.0x - No detections for 3 seconds")
                animateZoom(currentZoom, 1.0f) // Smooth zoom out to 1.0x
                zoomResetJob = null  // Reset job after zoom-out
            } else {
                Log.d(TAG, "❌ Object detected before timeout, canceling zoom-out")
            }
        }
    }

    /**
     * Smoothly animates the camera pan (translation)
     */
    private fun animatePan(currentX: Float, currentY: Float, targetX: Float, targetY: Float) {
        val smoothPanX = currentX + (targetX - currentX) * 0.2f
        val smoothPanY = currentY + (targetY - currentY) * 0.2f

        // Apply the updated camera position
        updateCameraPosition(smoothPanX, smoothPanY)
    }

    private fun updateCameraPosition(panX: Float, panY: Float) {
        cameraControl?.setZoomRatio(currentZoom) // Ensure zoom is updated

        // Apply panning (translation) using CameraX transformation
        previewView.scaleX = 1f + panX * 0.1f // Scale to move horizontally
        previewView.scaleY = 1f + panY * 0.1f // Scale to move vertically
    }

    companion object {
        const val THROTTLE_TIMEOUT_MS = 15_000L  // 1 minute throttle time
        const val TAG = "ObjectDetectionAnalyzer"
    }
}
