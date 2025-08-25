package com.example.zakazivanjeaplikacija.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("email")
    val email: String, // Email je obavezan
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("role")
    val role: String? = null // Može biti USER ili ADMIN
)