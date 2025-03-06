package com.spmadrid.vrepo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.presentation.ui.theme.Gray800


@Composable
fun ConductionResultCard(
    plateNumber: String,
    vehicleModel: String,
    chCode: String,
    endoDate: String,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Gray800
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row {
                Text(
                    text = "Status: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (status == "POSITIVE") Color.Red else Color.Yellow
                )
            }

            Text(
                text = "Plate Number: $plateNumber",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "Vehicle Model: $vehicleModel",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "CH Code: $chCode",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "Endo Date: $endoDate",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

