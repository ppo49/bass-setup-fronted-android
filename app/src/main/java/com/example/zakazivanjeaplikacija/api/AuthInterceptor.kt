package com.example.zakazivanjeaplikacija.api

import com.example.zakazivanjeaplikacija.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response


 //Interceptor koji dodaje JWT token u Authorization
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Dohvati token
        val token = tokenManager.getToken()
        val originalRequest = chain.request() // Originalni zahtev

        // Ako postoji token, dodaj ga u Authorization zaglavlje
        val requestBuilder = originalRequest.newBuilder()
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}
