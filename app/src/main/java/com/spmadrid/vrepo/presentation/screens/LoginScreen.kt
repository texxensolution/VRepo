package com.spmadrid.vrepo.presentation.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.R
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.presentation.components.ShiningFloatingNotification
import com.spmadrid.vrepo.presentation.ui.theme.Roboto
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel



@Composable
fun LoginScreen(
    context: Context,
    cameraViewModel: CameraViewModel,
    authViewModel: AuthenticateViewModel
) {
//    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val activity = context as Activity
    val showNotification = cameraViewModel.showNotification.collectAsState()
    val notification = cameraViewModel.notification.collectAsState()

    val buttonColor = if (isPressed) Color.Blue.copy(0.5f) else Color.Blue.copy(0.02f)

    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        notification.value?.let {
            ShiningFloatingNotification(
                showNotification = showNotification.value,
                context = context,
                notificationEvent = it
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.75f)
                .padding(horizontal = 12.dp, vertical = 32.dp)
                .background(color = Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(12.dp))
                .clip(shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color.White, RoundedCornerShape(12.dp)), // Add a border,
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "VRepo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    fontFamily = Roboto,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 10.dp),
                    color = Color.Black
                )

                Image(
                    painter = painterResource(R.drawable.eye_logo),
                    contentDescription = "VRepo Logo",
                    modifier = Modifier.fillMaxWidth(0.6f),
                    alignment = Alignment.Center
                )
            }


            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.Black,
                ),
                border = BorderStroke(2.dp, Color.Blue),
                onClick = {
                    authViewModel.signInWithLark(activity)
                },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.lark_logo),
                        contentDescription = "VRepo Logo",
                        modifier = Modifier.padding(vertical = 4.dp),
                        alignment = Alignment.Center
                    )

                    Spacer(modifier = Modifier.padding(end = 8.dp))

                    Text(
                        "Login with Lark",
                        color = Color.Blue,
                        fontFamily = Roboto
                    )
                }
            }
        }
    }
}