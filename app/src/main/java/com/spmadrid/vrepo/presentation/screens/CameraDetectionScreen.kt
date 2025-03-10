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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.LifecycleOwner
import com.spmadrid.vrepo.camera.ObjectDetectionAnalyzer
import com.spmadrid.vrepo.domain.dtos.DetectedTextResult
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.domain.services.LocationManagerService
import com.spmadrid.vrepo.domain.services.ServerInfoService
import com.spmadrid.vrepo.presentation.components.OpenStreetMapView
import com.spmadrid.vrepo.presentation.components.ServerStatusIndicator
import com.spmadrid.vrepo.presentation.components.ShiningFloatingNotification
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Compress
import compose.icons.fontawesomeicons.solid.DoorOpen
import compose.icons.fontawesomeicons.solid.Expand
import compose.icons.fontawesomeicons.solid.PowerOff
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun CameraDetectionScreen(
    objectDetector: IObjectDetector,
    cameraViewModel: CameraViewModel,
    authViewModel: AuthenticateViewModel,
    serverInfoService: ServerInfoService,
    locationManagerService: LocationManagerService
) {
        CameraDetectionContent(
            objectDetector,
            cameraViewModel = cameraViewModel,
            authViewModel = authViewModel,
            serverInfoService = serverInfoService,
            locationManagerService = locationManagerService
        )
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
private fun CameraDetectionContent(
    objectDetector: IObjectDetector,
    cameraViewModel: CameraViewModel,
    authViewModel: AuthenticateViewModel,
    serverInfoService: ServerInfoService,
    locationManagerService: LocationManagerService
) {
    val scope = rememberCoroutineScope()
    var isFullscreen by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }
    val notification by cameraViewModel.notification.collectAsState()
    val showNotification by cameraViewModel.showNotification.collectAsState()
    val detectedText by cameraViewModel.detectedText.collectAsState()
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }

    val window = (context as? android.app.Activity)?.window

    LaunchedEffect(Unit) {
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .then(if (isFullscreen) Modifier.fillMaxWidth().fillMaxHeight(0.9f) else Modifier.fillMaxWidth(0.4f).fillMaxHeight(0.3f) )
                .padding(10.dp)
//                .padding(bottom = 15.dp)
                .statusBarsPadding()
                .clip(RoundedCornerShape(16.dp)) // Rounded corners
                .border(2.dp, Color.White, RoundedCornerShape(16.dp)) // Border with rounded cornersr
                .zIndex(2f)
        ) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
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
                        previewViewRef.value = previewView
                        startObjectDetection(
                            cameraExecutor = cameraExecutor,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            objectDetector = objectDetector,
                            onDetectedText = { result ->
                                scope.launch {
                                    cameraViewModel.processing(
                                        text = result.text,
                                        detectedType = result.detectedType,
                                        frame = result.frame
                                    )
                                } },
                        )
                    }
                }
            )
            Button(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                onClick = {
                    isFullscreen = !isFullscreen
                    startObjectDetection(
                        cameraExecutor = cameraExecutor,
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewViewRef.value!!,
                        objectDetector = objectDetector,
                        onDetectedText = { result ->
                            scope.launch {
                                cameraViewModel.processing(
                                    text = result.text,
                                    detectedType = result.detectedType,
                                    frame = result.frame
                                )
                            }
                        })
                }
            ) {
                Icon(
                    imageVector = if (!isFullscreen) FontAwesomeIcons.Solid.Expand else FontAwesomeIcons.Solid.Compress,
                    contentDescription = "Full Screen",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        notification?.let {
            ShiningFloatingNotification(
                context = context,
                showNotification = showNotification,
                notificationEvent = it,
            )
        }

        if (!isFullscreen) {
            Column(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = 10.dp)
                .zIndex(15f)
            ) {
                ServerStatusIndicator(
                    isFullscreen = isFullscreen,
                    modifier = Modifier
                        .zIndex(15f)
                        .statusBarsPadding()
                        .padding(end = 10.dp)
                        .clip(RoundedCornerShape(10.dp)) // Rounded corners
                        .border(
                            2.dp,
                            Color.White,
                            RoundedCornerShape(10.dp)
                        ),
                    serverInfoService = serverInfoService
                )
                Button(
                    onClick = {
                        showDialog = true
                    },
                    modifier = Modifier
                        .padding(0.dp)
                        .padding(top = 10.dp)
                        .size(40.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp)) // Border stroke
                        .padding(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red // Change button background color
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Image(
                        imageVector = FontAwesomeIcons.Solid.PowerOff,
                        contentDescription = "logout button",
                        modifier = Modifier.size(18.dp).padding(0.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }

//        logout dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.clearToken()
                            showDialog = false
                            // Handle logout logic here
                        }
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }


        OpenStreetMapView(locationManagerService)

        if (detectedText.isNotBlank()) {
            detectedText.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(10f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullscreen) Color.White else Color.Black,
                        modifier = Modifier
                            .then(
                                if (isFullscreen) Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) else Modifier.background(
                                        Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
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
    onDetectedText: (DetectedTextResult) -> Unit,
) {
    cameraController.imageAnalysisResolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .build()
    cameraController.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
    cameraController.isTapToFocusEnabled = false
    cameraController.setImageAnalysisAnalyzer(
        cameraExecutor,
        ObjectDetectionAnalyzer(
            objectDetector = objectDetector,
            onDetectedText = onDetectedText,
        )
    )
    try {
        cameraController.unbind()
        cameraController.bindToLifecycle(lifecycleOwner)
    } catch (err: Exception) {
        err.printStackTrace()
    }

    previewView.controller = cameraController
}