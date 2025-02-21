package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spmadrid.vrepo.domain.services.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AuthenticateViewModel @Inject constructor(
    private val authenticationService: AuthenticationService
) : ViewModel() {
    fun signInWithLark() {
        authenticationService.openLarkSSO()
    }
}