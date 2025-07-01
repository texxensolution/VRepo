package com.spmadrid.vrepo.presentation.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.spmadrid.vrepo.domain.dtos.UserSummary
import com.spmadrid.vrepo.domain.repositories.UserSummaryRepository
import com.spmadrid.vrepo.presentation.ui.theme.Gray600
import com.spmadrid.vrepo.presentation.ui.theme.Gray800
import com.spmadrid.vrepo.presentation.ui.theme.Gray900
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SummaryOverlay(
    userSummaryRepository: UserSummaryRepository
) {
    // State management
    var context = LocalContext.current
    var summary by remember { mutableStateOf<UserSummary?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope tied to composable lifecycle
    val scope = rememberCoroutineScope()

    // Cancel any ongoing operation when composable leaves composition
    DisposableEffect(Unit) {
        val job = scope.launch {
            while (true) {
                try {
                    summary = userSummaryRepository.getUserSummary()
                } catch (err: Exception) {
                    errorMessage = err.message ?: "Unknown error occurred"
                    Log.e("SummaryOverlay", "Error fetching summary", err)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                } finally {
                    delay(5_000L) // Wait before next refresh
                }
            }
        }

        onDispose {
            job.cancel()
        }
    }


    Box(modifier = Modifier
        .zIndex(20f)
        .fillMaxSize()
        .padding(bottom = 102.dp, end = 32.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        summary?.let {
            Column {
                Text(text = "Total Count: ${it.totalCount}", color = Gray800, fontSize = 12.sp, fontWeight = FontWeight.Light)
                Text(text = "Unique Count: ${it.uniqueScannedCount}", color = Gray800, fontSize = 12.sp,  fontWeight = FontWeight.Light)
            }
        }
    }
}