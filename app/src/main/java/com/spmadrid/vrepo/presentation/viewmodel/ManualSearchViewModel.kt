package com.spmadrid.vrepo.presentation.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spmadrid.vrepo.domain.dtos.ClientDetailsResponse
import com.spmadrid.vrepo.domain.dtos.ManualNotifyGroupChatRequest
import com.spmadrid.vrepo.domain.dtos.PlateCheckInput
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.domain.services.LocationManagerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualSearchViewModel @Inject constructor(
    private val plateMatchingService: LicensePlateMatchingService,
    private val locationManagerService: LocationManagerService
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _searchResult: MutableStateFlow<ClientDetailsResponse?> = MutableStateFlow(null)
    val searchResult: StateFlow<ClientDetailsResponse?> = _searchResult

    fun setIsLoading() {
        _loading.value = true
    }

    fun doneLoading() {
        _loading.value = false
    }

    suspend fun search(targetText: String, detectedType: String): Unit {
        setIsLoading()

        if (targetText.isBlank()) {
            return
        }
        viewModelScope.launch {
            val location = locationManagerService.getCurrentLocation()
            location?.let {
                try {
                    val currentLocation = listOf(
                        location.latitude,
                        location.longitude
                    )

                    val input = PlateCheckInput(
                        plate = targetText,
                        detected_type = detectedType,
                        location = currentLocation
                    )

                    val matched = plateMatchingService.getClientDetails(input)
                    _searchResult.value = matched

                    when (matched?.status) {
                        "POSITIVE" -> {
                            Log.d("ManualSearchViewModel", "Matched: $matched")
                            val manualInput = ManualNotifyGroupChatRequest(
                                plate = matched.plate,
                                location = currentLocation,
                                detected_type = detectedType
                            )
                            val response = plateMatchingService.sendManualAlertToGroupChat(manualInput)
                        }
                        else -> {}
                    }

                } catch (err: Exception) {
                    Log.e("ManualSearchViewModel", "Error: $err")
                    doneLoading()
                } finally {
                    doneLoading()
                }
            }
        }
    }
}