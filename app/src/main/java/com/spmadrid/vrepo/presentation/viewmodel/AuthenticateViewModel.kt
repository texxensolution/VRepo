package com.spmadrid.vrepo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class AuthenticateViewModel @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val tokenManagerService: TokenManagerService
) : ViewModel() {
    val tokenState: StateFlow<String?> = tokenManagerService.tokenFlow

    fun storeToken(token: String) {
        tokenManagerService.saveToken(token)
    }

    fun clearToken() {
        tokenManagerService.clearToken()
    }

//    fun signInWithLark() {
//        authenticationService.openLarkSSO()
//    }
}