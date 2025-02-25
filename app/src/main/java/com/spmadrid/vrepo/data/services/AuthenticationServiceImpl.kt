package com.spmadrid.vrepo.data.services

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.spmadrid.vrepo.domain.repositories.AuthenticationRepository
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.ss.android.larksso.CallBackData
import com.ss.android.larksso.IGetDataCallback
import com.ss.android.larksso.LarkSSO
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@SuppressLint("HardwareIds")
class AuthenticationServiceImpl @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val context: Context,
) : AuthenticationService {
    private lateinit var builder: LarkSSO.Builder
    private lateinit var deviceId: String

    companion object {
        const val TAG = "AuthenticationServiceImpl"
    }

    override fun initialize(activity: Activity) {
        val scopeList = mutableListOf("contact:user.id:readonly")

        val _deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        builder = LarkSSO.Builder()
            .setAppId("cli_a639a4faacf8900a")
            .setServer("Lark")
            .setScopeList(ArrayList(scopeList))
            .setDeviceId(_deviceId)
            .setContext(activity)

        deviceId = builder.getDeviceId()
        Log.d(TAG, "Lark Device ID: $deviceId")
    }

    override suspend fun getAccessToken(code: String): String? {
        val response = authenticationRepository.signInWithLark(code)
        return response?.data?.access_token
    }

    override suspend fun getOAuthCodeFromLark(activity: Activity): String? {
        initialize(activity)
        return suspendCancellableCoroutine { continuation ->
            LarkSSO.inst().startSSOVerify(builder, object : IGetDataCallback {
                override fun onSuccess(data: CallBackData?) {
                    Log.d(TAG, "Code: ${data?.code.toString()}")
                    continuation.resume(data?.code.toString())
                }

                override fun onError(exc: CallBackData?) {
                    Log.d(TAG, exc.toString())
                    continuation.resume(null)
                }

            })
        }
    }
}