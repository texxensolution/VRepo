package com.spmadrid.vrepo.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spmadrid.vrepo.domain.dtos.BoundingBox
import com.spmadrid.vrepo.domain.dtos.NotificationEvent
import com.spmadrid.vrepo.domain.dtos.NotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.dtos.PlateStatus
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.domain.services.LocationManagerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CameraViewModel @Inject constructor(
    private val plateMatchingService: LicensePlateMatchingService,
    private val locationService: LocationManagerService
) : ViewModel(){
    private val _detectedText: MutableStateFlow<String> = MutableStateFlow("")
    val detectedText: StateFlow<String> = _detectedText

    fun updateDetectedText(
        text: String,
        DETECTED_DELAY: Long = 1_000L
    ) {
        viewModelScope.launch {
            _detectedText.value = text
            delay(DETECTED_DELAY)
            _detectedText.value = ""
        }
    }

    private val _notification: MutableStateFlow<NotificationEvent?> = MutableStateFlow(null)
    val notification: StateFlow<NotificationEvent?> = _notification

    fun updateNotification(notificationEvent: NotificationEvent) {
        viewModelScope.launch {
            _notification.value = notificationEvent
        }
    }

    private val _detectedBoundingBox: MutableStateFlow<BoundingBox?> = MutableStateFlow(null)
    val detectedBoundingBox: StateFlow<BoundingBox?> = _detectedBoundingBox

    fun updateBoundingBox(box: BoundingBox) {
        viewModelScope.launch(Dispatchers.Main) {
            Log.d("WhenBoundingBoxIsPresent", "$box")
            _detectedBoundingBox.value = box
        }
    }

    private val _showNotification: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showNotification: StateFlow<Boolean> = _showNotification

    fun notifyApp(
        notificationEvent: NotificationEvent,
        timeDelay: Long = 5_000L
    ) {
        viewModelScope.launch {
            _showNotification.value = true
            updateNotification(notificationEvent)
            delay(timeDelay)
            _showNotification.value = false
        }
    }

    fun processing(
        text: String,
        detectionType: String,
        frame: ByteArray,
        metadata: Map<String, String>?
    ) {
        updateDetectedText(text)

        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            if (location == null) {
                Log.d(TAG, "Location is null!")
                return@launch
            }

            val details = PlateCheckInput(
                plate = text,
                detection_type = detectionType,
                location = listOf(location.latitude, location.longitude),
                metadata = metadata
            )

            try {
                val response = plateMatchingService.getPlateStatus(details)

                when(response.status) {
                    PlateStatus.POSITIVE -> {
                        notifyApp(NotificationEvent(text, response.priority.toString()))
                        plateMatchingService.sendAlertToGroupChat(
                            NotifyGroupChatRequest(
                                plate = text,
                                image = frame,
                                detectionType = detectionType,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    }
                    PlateStatus.FOR_CONFIRMATION -> {
                        plateMatchingService.sendAlertToGroupChat(
                            NotifyGroupChatRequest(
                                plate = text,
                                image = frame,
                                detectionType = detectionType,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    }
                    PlateStatus.NEGATIVE -> {
                        Log.d("Plate Scanned Status", "Negative")
                    }
                }
            } catch (err: Exception) {
                Log.d(TAG, "HTTP Request: $err")
            }
        }
    }

    companion object {
        const val TAG = "CameraViewModel"
    }
}