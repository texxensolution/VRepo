package com.spmadrid.vrepo.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.spmadrid.vrepo.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun playSoundAndVibrate(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.buzz)
    if (!mediaPlayer.isPlaying) {
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }
    // Vibrate
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
}


fun CoroutineScope.repeatSoundAndVibrateSmoothlyFor30Sec(context: Context): Job {
    return launch(Dispatchers.IO) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        var mediaPlayer: MediaPlayer? = null

        try {
            val startTime = System.currentTimeMillis()
            val timings = longArrayOf(0, 300, 100, 300)
            val amplitudes = intArrayOf(100, 255, 0, 255)

            while (System.currentTimeMillis() - startTime < 10_000) {
                withContext(Dispatchers.Main) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1) // no repeat!
                        vibrator.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(timings, -1)
                    }

                    mediaPlayer = MediaPlayer.create(context, R.raw.buzz)
                    mediaPlayer?.setOnCompletionListener {
                        it.release()
                    }
                    mediaPlayer?.start()
                }

                delay(mediaPlayer?.duration?.toLong() ?: 500L)
            }
        } catch (e: CancellationException) {
            // Coroutine was cancelled (e.g., showNotification = false)
        } finally {
            withContext(Dispatchers.Main) {
                vibrator.cancel()
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
    }
}
