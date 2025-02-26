package com.spmadrid.vrepo.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import com.spmadrid.vrepo.R
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("SuspiciousIndentation")
@Composable
fun ShiningFloatingNotification(
    context: Context,
    showNotification: Boolean,
    notificationEvent: NotificationEvent,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scope = rememberCoroutineScope()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var vibrator by remember { mutableStateOf<Vibrator?>(null) }

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

        LaunchedEffect(showNotification) {
            if (showNotification) {
                scope.launch {
                    vibrator = triggerContinuousVibration(context)
                    mediaPlayer = playLoopingBuzzSound(context)
                }
                delay(3000) // Auto-dismiss after 3 seconds
                vibrator?.cancel()
                mediaPlayer?.stop()
                mediaPlayer?.release()
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
                .border(4.dp, gradientBrush, RoundedCornerShape(12.dp)) // Shining gradient border
                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🚨 PLATE: ${notificationEvent.plate} - POSITIVE 🚨",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun triggerContinuousVibration(context: Context): Vibrator {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val pattern = longArrayOf(0, 500, 500)
    val vibrationEffect = VibrationEffect.createWaveform(pattern, 0)
    vibrator.vibrate(vibrationEffect)
    return vibrator
}

fun playLoopingBuzzSound(context: Context): MediaPlayer {
    val mediaPlayer = MediaPlayer.create(context, R.raw.buzz)
    mediaPlayer.isLooping = true
    mediaPlayer.start()
    return mediaPlayer
}