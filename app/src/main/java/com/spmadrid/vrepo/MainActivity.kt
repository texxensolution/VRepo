package com.spmadrid.vrepo

import android.Manifest
import org.osmdroid.config.Configuration
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.spmadrid.vrepo.domain.dtos.BottomNavItem
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.domain.services.LocationManagerService
import com.spmadrid.vrepo.domain.services.ServerInfoService
import com.spmadrid.vrepo.presentation.components.BottomNavigationBar
import com.spmadrid.vrepo.presentation.screens.CameraDetectionScreen
import com.spmadrid.vrepo.presentation.screens.ConductionStickerScreen
import com.spmadrid.vrepo.presentation.screens.LoginScreen
import com.spmadrid.vrepo.presentation.screens.PermissionScreen
import com.spmadrid.vrepo.presentation.ui.theme.VRepoTheme
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import com.ss.android.larksso.LarkSSO
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.firstOrNull
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

    @Inject
    lateinit var serverInfoService: ServerInfoService

    @Inject
    lateinit var locationManagerService: LocationManagerService

    val cameraViewModel: CameraViewModel by viewModels()
    val authViewModel: AuthenticateViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        LarkSSO.inst().parseIntent(this, intent)
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            LarkSSO.inst().parseIntent(this, it)
            Log.d("MainActivity", it.data.toString())
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "VRepo"
//        enableEdgeToEdge()
        setContent {
            VRepoTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

                val locationPermissionState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                val tokenState = authViewModel.tokenState.collectAsState()

                Scaffold(
                    bottomBar = {
                        if (currentRoute != "permission") {
                            BottomNavigationBar(navController)
                        }
                    }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if(isPermissionDenied(cameraPermissionState, locationPermissionState)) "permission" else BottomNavItem.Home.route,
                    ) {
                        composable("permission") {
                            PermissionScreen(
                                cameraPermissionState,
                                locationPermissionState
                            )
                        }
                        composable(BottomNavItem.Home.route) {
                            CameraDetectionScreen(
                                objectDetector = objectDetector,
                                cameraViewModel = cameraViewModel,
                                authViewModel = authViewModel,
                                serverInfoService = serverInfoService,
                                locationManagerService = locationManagerService
                            )
                        }
                        composable(BottomNavItem.Conduction.route) {
                            ConductionStickerScreen(plateMatchingService = licensePlateMatchingService, locationManagerService = locationManagerService)
                        }
                    }

//                    if (!cameraPermissionState.status.isGranted || !locationPermissionState.allPermissionsGranted) {
//                        PermissionScreen(
//                            cameraPermissionState,
//                            locationPermissionState
//                        )
//                    } else {
//                        if (tokenState.value == null) {
//                            LoginScreen(
//                                context = this,
//                                cameraViewModel = cameraViewModel,
//                                authViewModel = authViewModel
//                            )
//                        } else {
//                            CameraDetectionScreen(
//                                objectDetector = objectDetector,
//                                cameraViewModel = cameraViewModel,
//                                authViewModel = authViewModel,
//                                serverInfoService = serverInfoService,
//                                locationManagerService = locationManagerService
//                            )
//                        }
//                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun isPermissionDenied(
    cameraPermissionState: PermissionState,
    locationPermissionState: MultiplePermissionsState
): Boolean {
    return !cameraPermissionState.status.isGranted || !locationPermissionState.allPermissionsGranted
}