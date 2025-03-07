package com.spmadrid.vrepo.presentation.hooks

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberVibrator(context: Context): Vibrator? {
    return remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use VibratorManager on Android 12+ (API 31+)
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            // Use Vibrator on older versions
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}