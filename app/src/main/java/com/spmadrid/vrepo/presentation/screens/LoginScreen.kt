package com.spmadrid.vrepo.presentation.screens

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel

@Composable
fun LoginScreen(
    authenticationService: AuthenticationService
) {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        activity?.let { authenticationService.initialize(activity) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(
            modifier = Modifier
                .padding(8.dp),
            onClick = {
                authenticationService.openLarkSSO()
            }
        ) {
            Text("Login with Lark")
        }
    }
}