package com.spmadrid.vrepo.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    cameraPermissionState: PermissionState,
    locationPermissionState: MultiplePermissionsState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚙ Enable Required Permission",
                fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(bottom = 22.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 52.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text("Camera Permission Granted: ")
            Text(if (cameraPermissionState.status.isGranted) "✅" else "❌")
        }

        Spacer(modifier = Modifier.padding(top = 8.0.dp))

        if (!cameraPermissionState.status.isGranted) {
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request Camera Permission")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 52.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text("Location Permission Granted: ")
            Text(if (locationPermissionState.allPermissionsGranted) "✅" else "❌")
        }

        Spacer(modifier = Modifier.padding(top = 10.0.dp))

        if (!locationPermissionState.allPermissionsGranted) {
            Button(onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                Text("Request Location Permissions")
            }
        }
    }
}