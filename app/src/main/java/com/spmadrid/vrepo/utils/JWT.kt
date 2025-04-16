package com.spmadrid.vrepo.utils

import android.util.Log
import com.auth0.jwt.JWT
import java.util.Date


const val TAG = "JWT"
object JWT {
    fun isTokenExpired(token: String): Boolean {
        try {
            val jwt = JWT.decode(token)

            val userId = jwt.getClaim("user_id")
            val user_type = jwt.getClaim("user_type")

            val isExpired = jwt.expiresAt.before(Date())

            Log.d(TAG, "JWT(userId=$userId, user_type=$user_type, isExpired=$isExpired)")
            return isExpired
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return false
        }
    }
}