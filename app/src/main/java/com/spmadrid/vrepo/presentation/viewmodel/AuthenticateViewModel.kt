package com.spmadrid.vrepo.presentation.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthenticateViewModel @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val tokenManagerService: TokenManagerService
) : ViewModel() {
    val tokenState: StateFlow<String?> = tokenManagerService.tokenFlow

    fun clearToken() {
        tokenManagerService.clearToken()
    }

    fun signInWithLark(activity: Activity) {
        viewModelScope.launch {
            val code = authenticationService.getOAuthCodeFromLark(activity)
            if (code != null) {
                val token = authenticationService.getAccessToken(code)
                if (token != null) {
                    tokenManagerService.saveToken(token)
                    Log.d("TokenManager", "Token saved: $token")
                }
            }
        }
    }
}