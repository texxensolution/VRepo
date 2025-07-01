package com.spmadrid.vrepo.domain.dtos

import kotlinx.serialization.Serializable


@Serializable
data class AuthenticateDataField(
    val access_token: String,
    val token_type: String
)

@Serializable
data class AuthenticateResponse(
    val status: String,
    val msg: String,
    val data: AuthenticateDataField?
)

@Serializable
data class Account(
    val plate_no: String,
    val vehicle_model: String,
    val client: String,
    val ch_code: String,
    val endo_date: String,
    val priority: String,
    val crm_code: String
)


//Response body
//Download
//{
//    "plate": "LAC8530",
//    "status": "FOR_CONFIRMATION",
//    "detection_type": "plates",
//    "accounts": [
//    {
//        "plate_no": "LAN8530",
//        "vehicle_model": "Avanza 13 J  MT WHITE",
//        "client": "TL3",
//        "ch_code": "02TFSA2503-39067",
//        "endo_date": "2025-03-28",
//        "priority": "HIGH",
//        "crm_code": "02TFSA2503-39067"
//    }
//    ],
//    "count": 1
//}

@Serializable
data class ClientDetailsResponse(
    val plate: String,
    val detection_type: String,
    val status: String,
    val accounts: List<Account>,
    val count: Int
)

@Serializable
data class PlateCheckInput(
    val plate: String,
    val detection_type: String,
    val location: List<Double>,
    val metadata: Map<String, String>?
)

@Serializable
data class NotifyGroupChatRequest(
    val plate: String,
    val image: ByteArray,
    val detectionType: String,
    val latitude: Double,
    val longitude: Double
)


@Serializable
data class ManualNotifyGroupChatRequest(
    val plate: String,
    val detection_type: String,
    val location: List<Double>
)

@Serializable
data class NotifyGroupChatResponse(
    val message: String,
    val type: String
)

@Serializable
data class CurrentDeviceLocation(
    val latitude: Double,
    val longitude: Double
)

data class GetPlateStatusResponse(
    val status: PlateStatus,
    val priority: String?
)