package com.spmadrid.vrepo.presentation.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun LoginScreen(
    context: Context,
    client: HttpClient,
    authenticationService: AuthenticationService,
    cameraViewModel: CameraViewModel,
    licensePlateMatchingService: LicensePlateMatchingService
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val showNotification = cameraViewModel.showNotification.collectAsState()
    val notification = cameraViewModel.notification.collectAsState()

    LaunchedEffect(Unit) {
        activity?.let {
            authenticationService.initialize(activity)
        }
    }



    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.bg),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        notification.value?.let {
            ShiningFloatingNotification(
                showNotification = showNotification.value,
                context = context,
                notificationEvent = it
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 32.dp)
                .background(color = Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(12.dp))
                .clip(shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color.White, RoundedCornerShape(12.dp)) // Add a border
        ) {
            Text(
                "\uD83D\uDC4B",
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
            )
            Text(
                "Welcome to VisionAI",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp).padding(top = 10.dp),
                color = Color.Black
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                onClick = { authenticationService.openLarkSSO() },
            ) {
                Text(
                    "Login with Lark",
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                modifier = Modifier,
                onClick = {
                    cameraViewModel.notifyApp(NotificationEvent("ABC1234"))
                }
            ) {
                Text("send an image example!")
            }
        }
    }
}

@OptIn(InternalAPI::class)
suspend fun exampleRequestWithImage(client: HttpClient, context: Context) {
    val assetManager = context.assets
    val inputStream = assetManager.open("test-img.jpg")
    val file = inputStream.readBytes()
    withContext(Dispatchers.IO) {
        inputStream.close()
    }

    val latitude = "14.5995"
    val longitude = "120.9842"
    val plateNumber = "ABC1234"

    val response: HttpResponse = client.post {
        url {
            appendPathSegments("api", "v4", "upload")
        }
        contentType(ContentType.MultiPart.FormData)
        body = MultiPartFormDataContent(
            formData {
                append("image", file, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"test-img.jpg\"")
                })

                append("latitude", latitude)
                append("longitude", longitude)
                append("plate_number", plateNumber)
            }
        )
    }

    println("Response: ${response.toString()}") // Print response from server

    client.close()
}