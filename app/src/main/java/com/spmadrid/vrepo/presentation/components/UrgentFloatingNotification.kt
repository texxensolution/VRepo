package com.spmadrid.vrepo.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.spmadrid.vrepo.utils.repeatSoundAndVibrateSmoothlyFor30Sec
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
@Composable
fun UrgentFloatingNotification(
    context: Context,
    showNotification: Boolean,
) {
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "")
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(showNotification) {
        if (showNotification) {
            job?.cancel() // Cancel previous job if any
            job = scope.launch {
                repeatSoundAndVibrateSmoothlyFor30Sec(context)
            }
        } else {
            job?.cancel()
            job = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            job?.cancel()
            job = null
        }
    }    // Animate gradient offset to create a "shining" effect
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Large enough for a smooth transition
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color.Black, Color.Gray, Color.Black),
        start = Offset(animatedOffset, 0f),
        end = Offset(animatedOffset + 200f, 200f)
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(25f),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showNotification,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .fillMaxWidth(fraction = 0.95f)
                .border(4.dp, gradientBrush, RoundedCornerShape(12.dp)) // Shining gradient border
                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🚨 Buzz: Please contact your leader ASAP!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}