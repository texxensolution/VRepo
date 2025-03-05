package com.spmadrid.vrepo.presentation.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.exts.removeDiacritics
import com.spmadrid.vrepo.exts.removeSpecialCharacters
import com.spmadrid.vrepo.presentation.components.ConductionResultCard
import com.spmadrid.vrepo.presentation.ui.theme.Gray100
import com.spmadrid.vrepo.presentation.ui.theme.Gray300
import com.spmadrid.vrepo.presentation.ui.theme.Gray600
import com.spmadrid.vrepo.presentation.ui.theme.Gray800
import com.spmadrid.vrepo.presentation.ui.theme.Gray900
import com.spmadrid.vrepo.presentation.ui.theme.GrayBG
import com.spmadrid.vrepo.presentation.viewmodel.ManualSearchViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Microphone
import compose.icons.fontawesomeicons.solid.Search
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun ConductionStickerScreen(
    manualSearchViewModel: ManualSearchViewModel
) {
    var conductionTextFieldState by remember { mutableStateOf("") }
    val searchResult = manualSearchViewModel.searchResult.collectAsState()
    val loading by manualSearchViewModel.loading.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launcher to handle speech recognition result
    val speechRecognizerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val spokenText =
                    data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                if (spokenText != null) {
                    conductionTextFieldState = spokenText
                        .removeDiacritics()
                        .removeSpecialCharacters()
                        .uppercase() ?: ""
                    scope.launch {
                        manualSearchViewModel.search(
                            conductionTextFieldState,
                            "sticker"
                        )
                    }
                }
            } else {
                Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
            }
        }


    Box(modifier = Modifier
        .padding()
        .fillMaxSize()
        .background(color = GrayBG)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier) {
                Text(
                    text = "Manual Search",
                    color = Gray900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                OutlinedTextField(
                    value = conductionTextFieldState,
                    onValueChange = { conductionTextFieldState = it },
                    label = { Text("Conduction Sticker") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Gray900,
                        focusedLabelColor = Gray900,
                        focusedContainerColor = GrayBG,
                        focusedTextColor = Gray900,
                        cursorColor = Gray900,
                        unfocusedIndicatorColor = Gray300,
                        unfocusedContainerColor = Gray100,
                        unfocusedTextColor = Gray800,
                        unfocusedPlaceholderColor = Gray900,
                        focusedPlaceholderColor = Gray900,
                        unfocusedLabelColor = Gray600
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Row {
                    Button(
                        onClick = {
                            scope.launch {
                                manualSearchViewModel.search(
                                    conductionTextFieldState,
                                    "sticker"
                                )
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.widthIn(120.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray900,
                            contentColor = Color.White,
                            disabledContainerColor = Gray800
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = GrayBG,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    FontAwesomeIcons.Solid.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(6.dp))
                                Text("Search")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        },
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray900,
                            contentColor = Color.White,
                            disabledContainerColor = Gray800
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                FontAwesomeIcons.Solid.Microphone,
                                contentDescription = "Microphone",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Speech to Text")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            if (searchResult != null) {
                Column(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Search Results", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Gray900)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        searchResult.value?.let {
                            items(it.accounts) { result ->
                                ConductionResultCard(
                                    plateNumber = result.plate_no,
                                    vehicleModel = result.vehicle_model,
                                    chCode = result.ch_code,
                                    endoDate = result.endo_date,
                                    status = it.status
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}