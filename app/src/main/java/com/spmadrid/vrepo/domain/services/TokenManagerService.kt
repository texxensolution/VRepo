package com.spmadrid.vrepo.domain.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

@Singleton
class TokenManagerService @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE).also {
            Log.d(TAG, "SharedPreferences (plain) created successfully")
        }

    private val _tokenFlow = MutableStateFlow(getTokenInternal())
    val tokenFlow: StateFlow<String?> = _tokenFlow

    private fun getTokenInternal(): String? {
        return try {
            val token = sharedPreferences.getString(BEARER_TOKEN_KEY, null)
            Log.d(TAG, "Initial token loaded: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error reading initial token", e)
            null
        }
    }

    fun saveToken(token: String) {
        try {
            sharedPreferences.edit { putString(BEARER_TOKEN_KEY, token) }
            _tokenFlow.value = token
            Log.d(TAG, "Token saved: $token")

            Firebase.crashlytics.log("Token saved: ${token.take(10)}...")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token", e)
            Firebase.crashlytics.log("Error saving token: ${e.message}")
        }
    }

    fun getToken(): String? {
        return try {
            val token = sharedPreferences.getString(BEARER_TOKEN_KEY, null)
            Log.d(TAG, "Token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving token", e)
            null
        }
    }

    suspend fun isTokenExpired(): Boolean {
        val token = tokenFlow.first() ?: return true
        return try {
            val jwt: DecodedJWT = JWT.decode(token)
            val expiresAt: Date? = jwt.expiresAt
            val expired = expiresAt == null || expiresAt.before(Date())
            Log.d(TAG, "Token expiry check: $expired")
            expired
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding token", e)
            true
        }
    }

    fun clearToken() {
        try {
            sharedPreferences.edit { remove(BEARER_TOKEN_KEY) }
            _tokenFlow.value = null
            Log.d(TAG, "Token cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing token", e)
        }
    }

    companion object {
        private const val TAG = "TokenManagerService"
        const val SHARED_PREF_KEY = "UNSECURE_PREFS"
        const val BEARER_TOKEN_KEY = "BEARER_TOKEN"
    }
}
