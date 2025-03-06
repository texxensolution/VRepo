package com.spmadrid.vrepo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.presentation.components.ConductionResultCard
import com.spmadrid.vrepo.presentation.ui.theme.Blue100
import com.spmadrid.vrepo.presentation.ui.theme.Blue200
import com.spmadrid.vrepo.presentation.ui.theme.Blue300
import com.spmadrid.vrepo.presentation.ui.theme.Blue400
import com.spmadrid.vrepo.presentation.ui.theme.Blue50
import com.spmadrid.vrepo.presentation.ui.theme.Blue500
import com.spmadrid.vrepo.presentation.ui.theme.Blue600
import com.spmadrid.vrepo.presentation.ui.theme.Blue700
import com.spmadrid.vrepo.presentation.ui.theme.Blue800
import com.spmadrid.vrepo.presentation.ui.theme.Gray100
import com.spmadrid.vrepo.presentation.ui.theme.Gray200
import com.spmadrid.vrepo.presentation.ui.theme.Gray300
import com.spmadrid.vrepo.presentation.ui.theme.Gray600
import com.spmadrid.vrepo.presentation.ui.theme.Gray800
import com.spmadrid.vrepo.presentation.ui.theme.Gray900
import com.spmadrid.vrepo.presentation.ui.theme.GrayBG
import com.spmadrid.vrepo.presentation.ui.theme.Red400
import com.spmadrid.vrepo.presentation.viewmodel.ManualSearchViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Cross
import compose.icons.fontawesomeicons.solid.Eraser
import compose.icons.fontawesomeicons.solid.Search
import kotlinx.coroutines.launch


@Composable
fun ConductionStickerScreen(
    manualSearchViewModel: ManualSearchViewModel
) {
    val searchText by manualSearchViewModel.searchText.collectAsState()
    val searchResult = manualSearchViewModel.searchResult.collectAsState()
    val loading by manualSearchViewModel.loading.collectAsState()
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
                    value = searchText,
                    onValueChange = { manualSearchViewModel.setSearchText(it) },
                    label = { Text("Conduction Sticker") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {manualSearchViewModel.setSearchText("")}) {
                            Icon(
                                FontAwesomeIcons.Solid.Eraser,
                                contentDescription = "Search",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Blue400,
                        focusedLabelColor = Blue500,
                        focusedContainerColor = Blue50,
                        focusedTextColor = Blue800,
                        cursorColor = Blue600,
                        unfocusedIndicatorColor = Blue200,
                        unfocusedContainerColor = Blue50,
                        unfocusedTextColor = Blue800,
                        unfocusedPlaceholderColor = Blue800,
                        focusedPlaceholderColor = Blue800,
                        unfocusedLabelColor = Blue300,
                        focusedTrailingIconColor = Blue400,
                        unfocusedTrailingIconColor = Blue400.copy(0.75f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                manualSearchViewModel.search(
                                    searchText,
                                    "sticker"
                                )
                            }
                        },
                        enabled = searchText.isNotBlank() && !loading,
                        modifier = Modifier.widthIn(120.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue700,
                            contentColor = Color.White,
                            disabledContainerColor = Blue600.copy(0.7f),
                            disabledContentColor = Gray200
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
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            if (searchResult != null) {
                Column(modifier = Modifier, verticalArrangement = spacedBy(12.dp)) {
                    Row(horizontalArrangement = spacedBy(8.dp)) {
                        Text("Search Results", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Gray900)
                        if (searchResult.value?.count == 0) {
                            Text("(0 Found)", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Gray900)
                        }
                    }
                    LazyColumn(
                        verticalArrangement = spacedBy(12.dp),
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