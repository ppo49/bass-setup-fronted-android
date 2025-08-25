package com.example.zakazivanjeaplikacija.api

import com.example.zakazivanjeaplikacija.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<AuthResponse>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<AuthResponse>

    @GET("api/users")
    fun getUsers(): Call<List<User>>

    @GET("api/services")
    fun getServices(): Call<List<ServiceResponseDTO>>

    @POST("api/appointments")
    fun saveAppointment(@Body appointmentRequest: AppointmentRequestToServerDTO): Call<AppointmentResponseDTO>

    @GET("api/appointments")
    fun getAppointments(): Call<List<AppointmentResponseDTO>>

    @GET("api/appointments/{id}")
    fun getAppointmentById(@Path("id") id: Long): Call<AppointmentResponseDTO>

    @GET("api/appointments/user/{userId}")
    fun getAppointmentsByUserId(@Path("userId") userId: Long): Call<List<AppointmentResponseDTO>>

    @PUT("api/appointments/{id}/pay")
    fun markAppointmentAsPaid(@Path("id") id: Long): Call<AppointmentResponseDTO>

    @PUT("api/appointments/{id}")
    fun updateAppointment(@Path("id") id: Long, @Body appointmentDetails: AppointmentResponseDTO): Call<AppointmentResponseDTO>

    @DELETE("api/appointments/{id}")
    fun deleteAppointment(@Path("id") id: Long): Call<String>
}
