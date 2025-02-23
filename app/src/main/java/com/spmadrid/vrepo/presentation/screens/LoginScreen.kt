package com.spmadrid.vrepo.presentation.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    authenticationService: AuthenticationService,
    licensePlateMatchingService: LicensePlateMatchingService
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        activity?.let {
            authenticationService.initialize(activity)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            Text(
                "Welcome to VisionAI",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                onClick = { authenticationService.openLarkSSO() },
            ){
                Text("Login with Lark")
            }

            Button(
                modifier = Modifier
                    .fillMaxSize(),
                onClick = {
                    throw RuntimeException("Test Crash")
//                    scope.launch {
//                        val response = licensePlateMatchingService.getPlateDetails(PlateCheckInput(
//                            plate = "Y0V029",
//                            detected_type = "plate",
//                            location = listOf(
//                                12.112323,
//                                121.121321321
//                            )
//                        ))
//                        Log.d("GetPlateDetailsRequest", response.toString())
//                    }
                }
            ) {
                Text("Fetch Plate Details")
            }
        }
    }
}