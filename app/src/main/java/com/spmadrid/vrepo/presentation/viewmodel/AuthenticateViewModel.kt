package com.spmadrid.vrepo.presentation.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthenticateViewModel @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val tokenManagerService: TokenManagerService
) : ViewModel() {
    val tokenState: StateFlow<String?> = tokenManagerService.tokenFlow

    private var _loading = MutableStateFlow(false)
    val loading = _loading

    private var _username = MutableStateFlow("")
    val username = _username

    private var _password = MutableStateFlow("")
    val password = _password

    fun updateUsername(username: String) {
        _username.value = username
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun clearToken() {
        tokenManagerService.clearToken()
    }

    fun signInWithLark(activity: Activity) {
        viewModelScope.launch {
            _loading.value = true
            val code = authenticationService.getOAuthCodeFromLark(activity)
            if (code != null) {
                val token = authenticationService.getAccessToken(code)
                if (token != null) {
                    tokenManagerService.saveToken(token)
                    Log.d("TokenManager", "Token saved: $token")
                }
            }
            _loading.value = false
        }
    }

    fun signInWithUsernameAndPassword(context: Context, username: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val token = authenticationService.signInWithUserAndPassword(username, password)
                if (token != null) {
                    tokenManagerService.saveToken(token)
                    Log.d("TokenManager", "Token saved: $token")
                    Toast.makeText(context, "Login Successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (err: Exception) {
                Log.e("Authentication", "Error: ${err.message}")
                Toast.makeText(context, "${err.message}", Toast.LENGTH_SHORT).show()
            }
            _loading.value = false
        }
    }
}