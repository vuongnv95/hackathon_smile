package com.example.core.model.network

import com.google.gson.annotations.SerializedName

class Login {
    data class Request(
        @SerializedName("identifier")
        val phoneNumber: String,
        @SerializedName("password")
        val password: String
    )

    data class Response(
        @SerializedName("token")
        val token: String? = null
    )
}