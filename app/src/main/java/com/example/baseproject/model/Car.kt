package com.example.baseproject.model

import java.io.Serializable

data class Car(
    val alcohol_concentration: Long = 0L,
    val id: String = "",
    val imei_alcohol: String = "",
    val name: String = "",
    val state: Int = 0
) : Serializable
