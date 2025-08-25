package com.example.zakazivanjeaplikacija

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import com.example.zakazivanjeaplikacija.api.ApiService
import com.example.zakazivanjeaplikacija.model.RegisterRequest
import com.example.zakazivanjeaplikacija.model.AuthResponse
import com.google.gson.GsonBuilder
import com.example.zakazivanjeaplikacija.utils.LocalDateTimeAdapter
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

class RegisterActivity : AppCompatActivity() {

    private val BASE_URL = "https://74b22c8d6899.ngrok-free.app/"
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

        val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = findViewById<EditText>(R.id.editTextLastName)
        val editTextEmailRegister = findViewById<EditText>(R.id.editTextEmailRegister)
        val editTextPasswordRegister = findViewById<EditText>(R.id.editTextPasswordRegister)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()
            val email = editTextEmailRegister.text.toString().trim()
            val password = editTextPasswordRegister.text.toString().trim()
            val role = "USER"

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Sva polja su obavezna!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val registerRequest = RegisterRequest(firstName, lastName, email, password, role)

            apiService.register(registerRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registracija uspešna!", Toast.LENGTH_SHORT).show()
                        Log.d("RegisterActivity", "Registracija uspešna za: $email")

                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@RegisterActivity, "Registracija neuspešna: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("RegisterActivity", "Registracija neuspešna! Kod: ${response.code()}, Greška: $errorBody")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Greška u mreži: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("RegisterActivity", "Greška u mreži prilikom registracije: ${t.message}", t)
                }
            })
        }
    }
}
