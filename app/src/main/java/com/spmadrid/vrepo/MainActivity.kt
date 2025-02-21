package com.spmadrid.vrepo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.presentation.screens.LoginScreen
import com.spmadrid.vrepo.presentation.ui.theme.VRepoTheme
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint()
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var objectDetector: IObjectDetector

    @Inject
    lateinit var authenticationService: AuthenticationService

    @Inject
    lateinit var licensePlateMatchingService: LicensePlateMatchingService

    val cameraViewModel: CameraViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VRepoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        authenticationService,
                        licensePlateMatchingService
                    )
//                    CameraDetectionScreen(
//                        objectDetector,
//                        cameraViewModel
//                    )
                }
            }
        }
    }
}