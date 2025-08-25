package com.example.zakazivanjeaplikacija.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDateTime


data class UserMinimal(
    val id: Long
)

data class ServiceMinimal(
    val id: Long
)


data class AppointmentRequestToServerDTO(
    val user: UserMinimal,
    val service: ServiceMinimal,
    val dateTime: LocalDateTime,
    val status: String
)



data class AppointmentResponseDTO(
    val id: Long,
    val userId: Long,
    val userEmail: String? = null,
    val service: ServiceResponseDTO,
    val dateTime: LocalDateTime,
    val status: String
)


