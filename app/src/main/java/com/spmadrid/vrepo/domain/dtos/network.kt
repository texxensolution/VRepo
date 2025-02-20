package com.spmadrid.vrepo.domain.dtos

data class AuthenticateDataField(
    val access_token: String,
    val token_type: String
)

data class AuthenticateResponse(
    val status: String,
    val msg: String,
    val data: AuthenticateDataField
)
