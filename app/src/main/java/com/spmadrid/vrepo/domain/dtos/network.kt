package com.spmadrid.vrepo.domain.dtos

import kotlinx.serialization.Serializable

data class AuthenticateDataField(
    val access_token: String,
    val token_type: String
)

data class AuthenticateResponse(
    val status: String,
    val msg: String,
    val data: AuthenticateDataField
)

@Serializable
data class Account(
    val plate_no: String,
    val vehicle_model: String,
    val ch_code: String,
    val endo_date: String
)

@Serializable
data class ClientDetailsResponse(
    val plate: String,
    val detected_type: String,
    val status: String,
    val accounts: List<Account>,
    val location: List<Double>,
    val count: Int
)

@Serializable
data class PlateCheckInput(
    val plate: String,
    val detected_type: String,
    val location: List<Double>
)
