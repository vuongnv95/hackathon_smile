package com.example.core.network

import com.example.core.model.network.Login
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiInterface {
    @POST("/api/app/login")
    suspend fun login(@Body request: Login.Request): Login.Response
}