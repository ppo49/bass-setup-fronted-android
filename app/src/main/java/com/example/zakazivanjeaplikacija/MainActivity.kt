package com.example.zakazivanjeaplikacija

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.example.zakazivanjeaplikacija.api.ApiService
import com.example.zakazivanjeaplikacija.api.AuthInterceptor
import com.example.zakazivanjeaplikacija.model.AuthResponse
import com.example.zakazivanjeaplikacija.model.LoginRequest
import com.example.zakazivanjeaplikacija.utils.LocalDateTimeAdapter
import com.example.zakazivanjeaplikacija.utils.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private val BASE_URL = "https://74b22c8d6899.ngrok-free.app/"
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(applicationContext)

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()

        val okHttpClient = OkHttpClient.Builder().build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonRegister = findViewById<Button>(R.id.buttonRegisterMain)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email i lozinka su obavezni!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)

            apiService.login(loginRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        authResponse?.let {
                            tokenManager.saveToken(it.token)
                            Toast.makeText(this@MainActivity, "Login uspešan!", Toast.LENGTH_SHORT).show()
                            Log.d("MainActivity", "Login uspešan! Token sačuvan.")
                            val intent = Intent(this@MainActivity, BookingActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@MainActivity, "Login neuspešan! Proverite kredencijale.", Toast.LENGTH_LONG).show()
                        Log.e("MainActivity", "Login neuspešan! Kod: ${response.code()}, Greška: $errorBody")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Greška u mreži: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("MainActivity", "Greška u mreži prilikom logina: ${t.message}", t)
                }
            })
        }


        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}
