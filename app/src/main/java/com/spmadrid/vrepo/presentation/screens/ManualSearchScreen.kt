package com.spmadrid.vrepo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.presentation.components.ConductionResultCard
import com.spmadrid.vrepo.presentation.ui.theme.Gray100
import com.spmadrid.vrepo.presentation.ui.theme.Gray300
import com.spmadrid.vrepo.presentation.ui.theme.Gray600
import com.spmadrid.vrepo.presentation.ui.theme.Gray900
import com.spmadrid.vrepo.presentation.ui.theme.GrayBG
import com.spmadrid.vrepo.presentation.viewmodel.ManualSearchViewModel
import kotlinx.coroutines.launch


@Composable
fun ConductionStickerScreen(
    manualSearchViewModel: ManualSearchViewModel
) {
    var conductionTextFieldState by remember { mutableStateOf("") }
    val searchResult = manualSearchViewModel.searchResult.collectAsState()
    val loading = manualSearchViewModel.loading.collectAsState()
    val scope = rememberCoroutineScope()


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
                        unfocusedTextColor = GrayBG,
                        unfocusedPlaceholderColor = Gray900,
                        focusedPlaceholderColor = Gray900,
                        unfocusedLabelColor = Gray600
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            manualSearchViewModel.search(
                                conductionTextFieldState,
                                "sticker"
                            )
                        }
                    },
                    modifier = Modifier.widthIn(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray900,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (loading.value) {
                        CircularProgressIndicator(
                            color = GrayBG,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Search")
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