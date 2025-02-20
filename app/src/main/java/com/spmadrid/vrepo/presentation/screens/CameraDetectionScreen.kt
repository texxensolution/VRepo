package com.spmadrid.vrepo.presentation.screens

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.spmadrid.vrepo.camera.ObjectDetectionAnalyzer
import com.spmadrid.vrepo.domain.dtos.BoundingBox
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import com.spmadrid.vrepo.domain.repositories.IObjectDetector
import com.spmadrid.vrepo.presentation.components.PopupNotification
import com.spmadrid.vrepo.presentation.components.ShiningFloatingNotification
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraDetectionScreen(
    objectDetector: IObjectDetector,
    cameraViewModel: CameraViewModel
) {
    CameraDetectionContent(
        objectDetector,
        cameraViewModel = cameraViewModel
    )
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
private fun CameraDetectionContent(
    objectDetector: IObjectDetector,
    cameraViewModel: CameraViewModel
) {
    val notification by cameraViewModel.notification.collectAsState()
    val showNotification by cameraViewModel.showNotification.collectAsState()
    val detectedText by cameraViewModel.detectedText.collectAsState()
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also { previewView ->
                    startObjectDetection(
                        cameraExecutor = cameraExecutor,
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        objectDetector = objectDetector,
                        onDetectedText = { text -> cameraViewModel.updateDetectedText(text) },
                        onNotifyApp = { notificationEvent -> cameraViewModel.notifyApp(notificationEvent) }
                    )
                }
            }
        )

        notification?.let {
            ShiningFloatingNotification(
                showNotification,
                notificationEvent = it,
            )
        }

        if (detectedText.isNotBlank()) {
            detectedText.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

private fun startObjectDetection(
    cameraExecutor: ExecutorService,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    objectDetector: IObjectDetector,
    onDetectedText: (String) -> Unit,
    onNotifyApp: (NotificationEvent) -> Unit
) {
    cameraController.imageAnalysisResolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .build()
    cameraController.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
    cameraController.setImageAnalysisAnalyzer(
        cameraExecutor,
        ObjectDetectionAnalyzer(
            objectDetector = objectDetector,
            onDetectedText = onDetectedText,
            onNotifyApp = onNotifyApp
        )
    )
    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}