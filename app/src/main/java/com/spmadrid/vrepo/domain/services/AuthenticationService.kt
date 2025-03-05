package com.spmadrid.vrepo.domain.services

import android.app.Activity

interface AuthenticationService {
    suspend fun getOAuthCodeFromLark(activity: Activity): String?
    suspend fun getAccessToken(code: String): String?
    suspend fun signInWithUserAndPassword(username: String, password: String): String?
    fun initialize(activity: Activity)
}