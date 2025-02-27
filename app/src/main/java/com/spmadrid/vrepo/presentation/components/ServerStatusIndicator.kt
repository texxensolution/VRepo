package com.spmadrid.vrepo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.spmadrid.vrepo.domain.services.ServerInfoService
import kotlinx.coroutines.delay

@Composable
fun ServerStatusIndicator(
    isFullscreen: Boolean,
    modifier: Modifier,
    serverInfoService: ServerInfoService
) {
    var serverStatus by remember { mutableStateOf(false) }
    val SERVER_CHECK_DELAY = 5000L

    LaunchedEffect(serverStatus) {
        while (true) {
            serverStatus = try {
                serverInfoService.isServerRunning()
            } catch (exc: Exception) {
                false
            }
            delay(SERVER_CHECK_DELAY)
        }
    }

    if (!isFullscreen) {
        Box(
            modifier = modifier
        ) {
            Text(
                if(serverStatus) "UP" else "DOWN",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .then(
                        if(serverStatus) Modifier.background(
                            color = Color.Green.copy(0.8f)
                        ) else Modifier.background(
                            color = Color.Red.copy(0.8f)
                        )
                    )
                    .padding(8.dp)
            )
        }
    }
}