package com.example.zakazivanjeaplikacija.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String
)