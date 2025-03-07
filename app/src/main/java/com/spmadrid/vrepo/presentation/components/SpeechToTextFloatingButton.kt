package com.spmadrid.vrepo.presentation.components

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import com.spmadrid.vrepo.domain.dtos.BottomNavItem
import com.spmadrid.vrepo.exts.removeDiacritics
import com.spmadrid.vrepo.exts.removeSpecialCharacters
import com.spmadrid.vrepo.presentation.hooks.rememberVibrator
import com.spmadrid.vrepo.presentation.ui.theme.Blue500
import com.spmadrid.vrepo.presentation.ui.theme.Blue800
import com.spmadrid.vrepo.presentation.viewmodel.ManualSearchViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Microphone
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SpeechToTextFloatingButton(
    currentRoute: String?,
    manualSearchViewModel: ManualSearchViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognitionIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Get results as the user speaks
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Change for different languages
        }
    }
    val vibrator = rememberVibrator(LocalContext.current)

    val scope = rememberCoroutineScope()

    var spokenText by remember { mutableStateOf("Press and Hold to Speak") }
    var isListening by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f, // Scale down when pressed
        animationSpec = tween(durationMillis = 100), label = "buttonScale"
    )

    val speechListener = remember {
        object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                spokenText = matches?.firstOrNull() ?: ""

                if (spokenText.isNotBlank()) {
                    scope.launch {
                        val text = spokenText
                            .removeDiacritics()
                            .removeSpecialCharacters()
                            .uppercase()
                        manualSearchViewModel.setSearchText(text)
                        val queryHaveResult = manualSearchViewModel.search(text, "sticker")
                        if (queryHaveResult && currentRoute != BottomNavItem.Conduction.route) {
                            navController.navigate(BottomNavItem.Conduction.route) {
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }  // Ensures back stack is preserved
                                restoreState = true  // Keeps UI state intact
                            }
                        }
                    }
                }
                Log.d("SpeechRecognition", "SpokenText: $spokenText")
            }

            override fun onError(error: Int) {
                isListening = false
                isPressed = false
                Log.e("SpeechRecognition", "Error: $error")
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                isPressed = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(speechListener)

    }

    Column(
        modifier = Modifier.offset(y = (60.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isListening) "Listening..." else "Hold to Speak",
            color = Blue800
        )
        Spacer(modifier = Modifier.height(8.dp))
        FloatingActionButton(
            onClick = {},
            modifier = Modifier
                .size(86.dp)
                .scale(scale)
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isListening = true
                            isPressed = true
                            spokenText = "Listening..."
                            // Trigger Vibration

                            vibrator?.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE)) // For older Android versions
                            speechRecognizer.startListening(recognitionIntent)
                        }
                        MotionEvent.ACTION_UP -> {
                            isListening = false
                            isPressed = false
                            speechRecognizer.stopListening()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            isListening = false
                            isPressed = false
                        }
                    }
                    true
                },
            shape = RoundedCornerShape(100.dp),
            containerColor = Blue500
        ) {
            Icon(
                FontAwesomeIcons.Solid.Microphone,
                contentDescription = "Microphone",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}