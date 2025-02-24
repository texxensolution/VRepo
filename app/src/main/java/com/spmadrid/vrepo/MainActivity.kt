package com.spmadrid.vrepo

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.presentation.screens.LoginScreen
import com.spmadrid.vrepo.presentation.screens.PermissionScreen
import com.spmadrid.vrepo.presentation.ui.theme.VRepoTheme
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import javax.inject.Inject

@AndroidEntryPoint()
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var objectDetector: IObjectDetector

    @Inject
    lateinit var authenticationService: AuthenticationService

    @Inject
    lateinit var licensePlateMatchingService: LicensePlateMatchingService

    @Inject
    lateinit var client: HttpClient

    val cameraViewModel: CameraViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VRepoTheme {
                val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
                val locationPermissionState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    if (!cameraPermissionState.status.isGranted || !locationPermissionState.allPermissionsGranted) {
                        PermissionScreen(
                            cameraPermissionState,
                            locationPermissionState)
                    } else {
                        LoginScreen(
                            context = this,
                            client = client,
                            authenticationService = authenticationService,
                            cameraViewModel = cameraViewModel,
                            licensePlateMatchingService = licensePlateMatchingService
                        )
                    }
//                    CameraDetectionScreen(
//                        objectDetector,
//                        cameraViewModel
//                    )
                }
            }
        }
    }
}