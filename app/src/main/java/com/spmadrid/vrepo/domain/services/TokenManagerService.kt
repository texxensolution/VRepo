package com.spmadrid.vrepo.domain.services

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManagerService @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        SHARED_PREF_KEY,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokenFlow = MutableStateFlow(sharedPreferences.getString(BEARER_TOKEN_KEY, null))
    val tokenFlow: StateFlow<String?> =  _tokenFlow

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(BEARER_TOKEN_KEY, token).apply()
        _tokenFlow.value = token
    }

    fun getToken(): String? {
        return sharedPreferences.getString(BEARER_TOKEN_KEY, null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove(BEARER_TOKEN_KEY).apply()
        _tokenFlow.value = null
    }

    companion object {
        const val SHARED_PREF_KEY = "SECURE_SHARED_PREFS"
        const val BEARER_TOKEN_KEY = "BEARER_TOKEN_KEY"
    }
}