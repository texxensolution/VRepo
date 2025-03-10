package com.spmadrid.vrepo.domain.repositories

import com.spmadrid.vrepo.domain.dtos.AuthenticateResponse

interface AuthenticationRepository {
    suspend fun signInWithLark(code: String): AuthenticateResponse?
    suspend fun signInWithUserAndPassword(username: String, password: String): AuthenticateResponse?
}