package com.example.zakazivanjeaplikacija.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ServiceResponseDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: BigDecimal
)