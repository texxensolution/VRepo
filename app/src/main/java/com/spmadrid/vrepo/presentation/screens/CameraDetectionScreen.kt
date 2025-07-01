package com.spmadrid.vrepo.presentation.screens

import com.spmadrid.vrepo.R
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.spmadrid.vrepo.camera.ObjectDetectionAnalyzer
import com.spmadrid.vrepo.domain.dtos.DetectedTextResult
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.domain.repositories.UserSummaryRepository
import com.spmadrid.vrepo.domain.services.LocationManagerService
import com.spmadrid.vrepo.presentation.components.OpenStreetMapView
import com.spmadrid.vrepo.presentation.components.ServerStatusIndicator
import com.spmadrid.vrepo.presentation.components.ShiningFloatingNotification
import com.spmadrid.vrepo.presentation.components.SummaryOverlay
import com.spmadrid.vrepo.presentation.components.UrgentFloatingNotification
import com.spmadrid.vrepo.presentation.ui.theme.Gray600
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import com.spmadrid.vrepo.presentation.viewmodel.PersistentSocketViewModel
import com.spmadrid.vrepo.presentation.viewmodel.RealtimeNotificationViewModel
import com.spmadrid.vrepo.presentation.viewmodel.UserInterfaceStateViewModel
import com.spmadrid.vrepo.utils.playSoundAndVibrate
import com.spmadrid.vrepo.utils.repeatSoundAndVibrateSmoothlyFor30Sec
//import com.spmadrid.vrepo.utils.playSoundQueued
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Compress
import compose.icons.fontawesomeicons.solid.Expand
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeSlash
import compose.icons.fontawesomeicons.solid.PowerOff
import compose.icons.fontawesomeicons.solid.WindowRestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun CameraDetectionScreen(
    objectDetector: IObjectDetector,
    cameraViewModel: CameraViewModel,
    authViewModel: AuthenticateViewModel,
    persistentSocketViewModel: PersistentSocketViewModel,
    userInterfaceStateViewModel: UserInterfaceStateViewModel,
    locationManagerService: LocationManagerService,
    triggerPictureInPictureMode: () -> Unit,
    userSummaryRepository: UserSummaryRepository
) {
        CameraDetectionContent(
            objectDetector,
            cameraViewModel = cameraViewModel,
            authViewModel = authViewModel,
            persistentSocketViewModel = persistentSocketViewModel,
            userInterfaceStateViewModel = userInterfaceStateViewModel,
            locationManagerService = locationManagerService,
            triggerPictureInPictureMode = triggerPictureInPictureMode,
            userSummaryRepository = userSummaryRepository
        )
}


@kotlin.OptIn(FlowPreview::class)
@SuppressLint("StateFlowValueCalledInComposition", "ClickableViewAccessibility")
@Composable
private fun CameraDetectionContent(
    objectDetector: IObjectDetector,
    cameraViewModel: CameraViewModel,
    authViewModel: AuthenticateViewModel,
    persistentSocketViewModel: PersistentSocketViewModel,
    userInterfaceStateViewModel: UserInterfaceStateViewModel,
    locationManagerService: LocationManagerService,
    triggerPictureInPictureMode: () -> Unit,
    userSummaryRepository: UserSummaryRepository
) {
    val scope = rememberCoroutineScope()
    var isFullscreen by remember { mutableStateOf(false) }
    var hideCameraFeed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var isAutoZoomEnabled by remember { mutableStateOf(false) }
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }
    var isBuzzed by remember { mutableStateOf(false) }

    val notification by cameraViewModel.notification.collectAsState()
    val showNotification by cameraViewModel.showNotification.collectAsState()
    val detectedText by cameraViewModel.detectedText.collectAsState()
    val isInPictureMode by userInterfaceStateViewModel.isInPictureMode.collectAsState()
    var zoomLevel by remember { mutableFloatStateOf(1f) }

    val cameraExecutor = Executors.newSingleThreadExecutor()
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    val window = (context as? android.app.Activity)?.window

    LaunchedEffect(Unit) {
        persistentSocketViewModel.incomingMessages
            .collect { incomingMessage ->
                isBuzzed = true

                delay(10_000)

                isBuzzed = false
                Log.d("Notification:Incoming-Message", incomingMessage)
            }
    }

    LaunchedEffect(isAutoZoomEnabled) {
        Log.d("RerenderAutoZoom", isAutoZoomEnabled.toString())
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
                        detectionType = result.detectedType,
                        frame = result.frame,
                        metadata = mapOf("trigger_type" to "camera_detection")
                    )
                }
            },
            isAutoZoomEnabled = isAutoZoomEnabled
        )
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(15f),
            contentAlignment = Alignment.Center
        ) {
            UrgentFloatingNotification(
                context = context,
                showNotification = isBuzzed
            )
        }

        Box(
            modifier = Modifier
                .then(
                    if (isFullscreen || isInPictureMode) Modifier.fillMaxSize() // For fullscreen or picture mode
                    else Modifier.fillMaxWidth(0.4f).fillMaxHeight(0.3f) // Otherwise, 0.3 height )
                )
                .then(
                    if (isFullscreen || isInPictureMode == false) {
                        Modifier.padding(9.dp)
                            .statusBarsPadding()
                            .clip(RoundedCornerShape(15.dp)) // Rounded corners
                            .border(
                                1.dp,
                                Color.White,
                                RoundedCornerShape(16.dp)
                            ) // Border with rounded cornersr
                            .zIndex(if (hideCameraFeed) -2f else 2f)
                    }
                    else Modifier
                )

        ) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { context ->
                    val previewView = PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }.also { previewView ->
                        previewViewRef.value = previewView
                    }
                    // Create GestureDetector
                    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            Log.d("Zooming", "Double tapped!")

                            val cameraControl = cameraController.cameraControl ?: return false
                            val minZoom = cameraController.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
                            val maxZoom = cameraController.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 3f

                            // Compute 3 evenly spaced zoom levels: minZoom, midZoom, maxZoom
                            val zoomLevels = listOf(minZoom, maxZoom / 2, maxZoom)
                            var currentZoomIndex = zoomLevels.indexOf(zoomLevel)
                            if (currentZoomIndex == -1) currentZoomIndex = 0

                            // Cycle through zoom levels
                            currentZoomIndex = (currentZoomIndex + 1) % zoomLevels.size
                            val targetZoom = zoomLevels[currentZoomIndex]
                            val currentZoom = zoomLevel
                            zoomLevel = targetZoom

                            Log.d("Zooming Ratio", "Target: $zoomLevel")

                            // Animate zooming smoothly
                            val animator = ValueAnimator.ofFloat(currentZoom, targetZoom).apply {
                                duration = 500 // 500ms for smooth transition
                                interpolator = DecelerateInterpolator()
                                addUpdateListener { animation ->
                                    val animatedZoom = animation.animatedValue as Float
                                    val linearZoom = (animatedZoom - minZoom) / (maxZoom - minZoom)
                                    cameraControl.setLinearZoom(linearZoom.coerceIn(0f, 1f))
                                }
                                start()
                            }
                            return true
                        }
                    })
                    previewView.setOnTouchListener { _, event ->
                        gestureDetector.onTouchEvent(event)
                    }
                    previewView
                }
            )
            if (!isInPictureMode) {
                Button(
                    modifier = Modifier
                        .align(alignment = if (isFullscreen) Alignment.BottomStart else Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .padding(start = if (isFullscreen) 12.dp else 0.dp),
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
                                        detectionType = result.detectedType,
                                        frame = result.frame,
                                        metadata = mapOf(
                                            "trigger_type" to "camera_detection"
                                        )
                                    )
                                }
                            },
                            isAutoZoomEnabled = isAutoZoomEnabled
                        )

                    }

                ) {
                    Icon(
                        imageVector = if (!isFullscreen) FontAwesomeIcons.Solid.Expand else FontAwesomeIcons.Solid.Compress,
                        contentDescription = "Full Screen",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }



        notification?.let {
            if (it.priority == "MEDIUM" || it.priority == "HIGH") {
                ShiningFloatingNotification(
                    context = context,
                    showNotification = showNotification,
                    notificationEvent = it,
                )
            }
        }
        if (isInPictureMode == false) {
            if (!isFullscreen ) {
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
    //                    serverInfoService = serverInfoService
                        persistentSocketViewModel = persistentSocketViewModel
                    )
                    Button(
                        onClick = {
                            hideCameraFeed = !hideCameraFeed
                        },
                        modifier = Modifier
                            .padding(0.dp)
                            .padding(top = 10.dp)
                            .size(40.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp)) // Border stroke
                            .padding(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!hideCameraFeed) Color.Green else Gray600 // Change button background color
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Image(
                            imageVector = if (!hideCameraFeed) FontAwesomeIcons.Solid.Eye else FontAwesomeIcons.Solid.EyeSlash,
                            contentDescription = "logout button",
                            modifier = Modifier.size(18.dp).padding(0.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
    //                turn off auto zoom
                    Button(
                        onClick = {
                            isAutoZoomEnabled = !isAutoZoomEnabled
                        },
                        modifier = Modifier
                            .padding(0.dp)
                            .padding(top = 10.dp)
                            .size(40.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp)) // Border stroke
                            .padding(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAutoZoomEnabled) Color.Green else Gray600 // Change button background color
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.telescope), // Replace with your drawable
                            contentDescription = "Vector Icon",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    // PIP MODE
                    Button(
                        onClick = {
                            triggerPictureInPictureMode()
                        },
                        modifier = Modifier
                            .padding(0.dp)
                            .padding(top = 10.dp)
                            .size(40.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp)) // Border stroke
                            .padding(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAutoZoomEnabled) Color.Green else Gray600 // Change button background color
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.telescope), // Replace with your drawable
//                            contentDescription = "Vector Icon",
//                            modifier = Modifier.size(24.dp),
//                            colorFilter = ColorFilter.tint(Color.White)
//                        )
                        Image(
                            imageVector = FontAwesomeIcons.Solid.WindowRestore,
                            contentDescription = "logout button",
                            modifier = Modifier.size(18.dp).padding(0.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }

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

//                SummaryOverlay(
//                    userSummaryRepository = userSummaryRepository
//                )
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
                            .zIndex(10f)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = text,
                            fontSize = 12.sp,
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
}

@OptIn(ExperimentalCamera2Interop::class)
private fun startObjectDetection(
    cameraExecutor: ExecutorService,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    objectDetector: IObjectDetector,
    previewView: PreviewView,
    onDetectedText: (DetectedTextResult) -> Unit,
    isAutoZoomEnabled: Boolean
) {
    cameraController.imageAnalysisResolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
        .build()
    cameraController.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
    cameraController.isTapToFocusEnabled = false

    val camera2Config = cameraController.cameraControl?.let { Camera2CameraControl.from(it) }

    if (camera2Config != null) {
        camera2Config.captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            .build()
    }

    cameraController.setImageAnalysisAnalyzer(
        cameraExecutor,
        ObjectDetectionAnalyzer(
            objectDetector = objectDetector,
            cameraControl = cameraController.cameraControl,
            previewView = previewView,
            onDetectedText = onDetectedText,
            isAutoZoomEnabled = isAutoZoomEnabled
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