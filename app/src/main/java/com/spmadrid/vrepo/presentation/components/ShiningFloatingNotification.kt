package com.spmadrid.vrepo.presentation.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import kotlinx.coroutines.delay


@Composable
fun ShiningFloatingNotification(
    showNotification: Boolean,
    notificationEvent: NotificationEvent,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Animate gradient offset to create a "shining" effect
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Large enough for a smooth transition
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color.Red, Color.Yellow, Color.Red),
        start = Offset(animatedOffset, 0f),
        end = Offset(animatedOffset + 200f, 200f)
    )

    if (showNotification) {
        LaunchedEffect(Unit) {
            delay(3000) // Auto-dismiss after 3 seconds
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = 40.dp)
    ) {
        AnimatedVisibility(
            visible = showNotification,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(fraction = 0.95f)
//                .fillMaxHeight(fraction = 0.8f)
                .border(4.dp, gradientBrush, RoundedCornerShape(12.dp)) // Shining gradient border
                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🚨 PLATE: NBC 1234 - POSITIVE 🚨",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}