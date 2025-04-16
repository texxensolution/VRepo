package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class UserInterfaceStateViewModel @Inject constructor() : ViewModel() {
    private val _isInPictureMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInPictureMode: StateFlow<Boolean> = _isInPictureMode

    fun setIsInPictureMode(pictureMode: Boolean) {
        _isInPictureMode.value = pictureMode
    }
}