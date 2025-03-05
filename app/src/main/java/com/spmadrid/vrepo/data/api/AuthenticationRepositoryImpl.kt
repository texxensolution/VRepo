package com.spmadrid.vrepo.data.api

import com.spmadrid.vrepo.domain.dtos.AuthenticateResponse
import com.spmadrid.vrepo.domain.repositories.AuthenticationRepository

class AuthenticationRepositoryImpl : AuthenticationRepository  {
    override suspend fun signInWithLark(code: String): AuthenticateResponse {
        TODO("Not yet implemented")
    }

    override suspend fun signInWithUserAndPassword(
        username: String,
        password: String
    ): AuthenticateResponse? {
        TODO("Not yet implemented")
    }
}