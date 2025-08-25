package com.example.zakazivanjeaplikacija

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazivanjeaplikacija.adapters.ServiceAdapter
import com.example.zakazivanjeaplikacija.adapters.SelectedServiceAdapter
import com.example.zakazivanjeaplikacija.api.ApiService
import com.example.zakazivanjeaplikacija.api.AuthInterceptor
import com.example.zakazivanjeaplikacija.model.AppointmentResponseDTO
import com.example.zakazivanjeaplikacija.model.ServiceResponseDTO
import com.example.zakazivanjeaplikacija.utils.TokenManager
import com.example.zakazivanjeaplikacija.utils.LocalDateTimeAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.zakazivanjeaplikacija.model.AppointmentRequestToServerDTO
import com.example.zakazivanjeaplikacija.model.UserMinimal
import com.example.zakazivanjeaplikacija.model.ServiceMinimal
import android.content.Intent
import android.app.DatePickerDialog
import android.widget.TextView
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private val BASE_URL = "https://74b22c8d6899.ngrok-free.app/" //promeni svaki put kada pokrenes ngrok

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService

    private lateinit var availableServicesAdapter: ServiceAdapter
    private lateinit var selectedServicesAdapter: SelectedServiceAdapter
    private val selectedServicesList: MutableList<ServiceResponseDTO> = mutableListOf()

    private lateinit var textViewSelectedDate: TextView
    private var selectedDateTime: LocalDateTime = LocalDateTime.now().toLocalDate().atStartOfDay()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        tokenManager = TokenManager(applicationContext)

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)

        val recyclerViewAvailableServices = findViewById<RecyclerView>(R.id.recyclerViewAvailableServices)
        availableServicesAdapter = ServiceAdapter(mutableListOf()) { service ->
            addServiceToSelection(service)
        }
        recyclerViewAvailableServices.layoutManager = LinearLayoutManager(this)
        recyclerViewAvailableServices.adapter = availableServicesAdapter

        val recyclerViewSelectedServices = findViewById<RecyclerView>(R.id.recyclerViewSelectedServices)
        selectedServicesAdapter = SelectedServiceAdapter(selectedServicesList) { service ->
            removeServiceFromSelection(service)
        }
        recyclerViewSelectedServices.layoutManager = LinearLayoutManager(this)
        recyclerViewSelectedServices.adapter = selectedServicesAdapter

        val buttonConfirmBooking = findViewById<Button>(R.id.buttonConfirmBooking)
        buttonConfirmBooking.setOnClickListener {
            confirmBooking()
        }

        val buttonLogout = findViewById<Button>(R.id.buttonLogout)
        buttonLogout.setOnClickListener {
            tokenManager.deleteToken()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Uspešno ste se odjavili!", Toast.LENGTH_SHORT).show()
        }

        textViewSelectedDate = findViewById(R.id.textViewSelectedDate)
        updateDateDisplay()

        textViewSelectedDate.setOnClickListener {
            showDatePickerDialog()
        }

        fetchAvailableServices()
    }

    private fun fetchAvailableServices() {
        apiService.getServices().enqueue(object : Callback<List<ServiceResponseDTO>> {
            override fun onResponse(call: Call<List<ServiceResponseDTO>>, response: Response<List<ServiceResponseDTO>>) {
                if (response.isSuccessful) {
                    val services = response.body()
                    services?.let {
                        availableServicesAdapter.updateServices(it)
                        Log.d("BookingActivity", "Dostupne usluge uspešno učitane: ${it.size}")
                    } ?: run {
                        Log.e("BookingActivity", "Dohvatanje usluga uspešno, ali lista je prazna.")
                        Toast.makeText(this@BookingActivity, "Nema dostupnih usluga.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("BookingActivity", "Dohvatanje usluga neuspešno! Kod: ${response.code()}, Greška: $errorBody")
                    Toast.makeText(this@BookingActivity, "Greška pri dohvatanju usluga: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<ServiceResponseDTO>>, t: Throwable) {
                Log.e("BookingActivity", "Greška prilikom dohvatanja usluga: ${t.message}", t)
                Toast.makeText(this@BookingActivity, "Greška u mreži pri dohvatanja usluga: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun addServiceToSelection(service: ServiceResponseDTO) {
        if (!selectedServicesList.contains(service)) {
            selectedServicesAdapter.addService(service)
            Toast.makeText(this, "${service.name} dodata u odabir.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "${service.name} je već odabrana.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeServiceFromSelection(service: ServiceResponseDTO) {
        selectedServicesAdapter.removeService(service)
        Toast.makeText(this, "${service.name} uklonjena iz odabira.", Toast.LENGTH_SHORT).show()
    }

    private fun confirmBooking() {
        if (selectedServicesList.isEmpty()) {
            Toast.makeText(this, "Molimo odaberite barem jednu uslugu.", Toast.LENGTH_SHORT).show()
            return
        }

        val now = LocalDateTime.now().toLocalDate().atStartOfDay()
        if (selectedDateTime.isBefore(now)) {
            Toast.makeText(this, "Ne možete zakazati termin u prošlosti!", Toast.LENGTH_LONG).show()
            Log.w("BookingActivity", "Pokušaj zakazivanja termina u prošlosti: $selectedDateTime")
            return
        }



        val userIdFromToken = extractUserIdFromToken(tokenManager.getToken())

        if (userIdFromToken == null) {
            Toast.makeText(this, "Greška: Korisnik nije pravilno ulogovan ili ID nije dostupan.", Toast.LENGTH_LONG).show()
            Log.e("BookingActivity", "UserID nije pronađen u tokenu ili token ne postoji.")
            return
        }

        selectedServicesList.forEach { service ->
            val appointmentRequest = AppointmentRequestToServerDTO(
                user = UserMinimal(id = userIdFromToken),
                service = ServiceMinimal(id = service.id),
                dateTime = selectedDateTime,
                status = "PENDING"
            )

            apiService.saveAppointment(appointmentRequest).enqueue(object : Callback<AppointmentResponseDTO> {
                override fun onResponse(call: Call<AppointmentResponseDTO>, response: Response<AppointmentResponseDTO>) {
                    if (response.isSuccessful) {
                        Log.d("BookingActivity", "Termin za ${service.name} uspešno zakazan!")
                        Toast.makeText(this@BookingActivity, "Zakazan termin za: ${service.name} ${selectedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))}", Toast.LENGTH_LONG).show()
                        selectedServicesAdapter.removeService(service)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("BookingActivity", "Greška pri zakazivanju termina za ${service.name}: Kod: ${response.code()}, Greška: $errorBody")
                        Toast.makeText(this@BookingActivity, "Greška pri zakazivanju ${service.name}: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AppointmentResponseDTO>, t: Throwable) {
                    Log.e("BookingActivity", "Greška u mreži pri zakazivanju termina za ${service.name}: ${t.message}", t)
                    Toast.makeText(this@BookingActivity, "Greška u mreži pri zakazivanju ${service.name}: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun extractUserIdFromToken(token: String?): Long? {
        token ?: return null
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("BookingActivity", "Neispravan JWT format.")
                return null
            }
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
            val jsonObject = com.google.gson.JsonParser.parseString(payload).asJsonObject
            return jsonObject.get("userId")?.asLong
        } catch (e: Exception) {
            Log.e("BookingActivity", "Greška pri parsiranju tokena: ${e.message}", e)
            return null
        }
    }

    private fun extractUserEmailFromToken(token: String?): String? {
        token ?: return null
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("BookingActivity", "Neispravan JWT format.")
                return null
            }
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
            val jsonObject = com.google.gson.JsonParser.parseString(payload).asJsonObject
            return jsonObject.get("sub")?.asString
        } catch (e: Exception) {
            Log.e("BookingActivity", "Greška pri parsiranju tokena (email): ${e.message}", e)
            return null
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance().apply {
            set(selectedDateTime.year, selectedDateTime.monthValue - 1, selectedDateTime.dayOfMonth)
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                val dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK)

                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(this, "Subota i Nedelja nisu dostupne za zakazivanje!", Toast.LENGTH_LONG).show()
                } else {
                    selectedDateTime = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDayOfMonth, 0, 0, 0)
                    updateDateDisplay()
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateDisplay() {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
        textViewSelectedDate.text = "Datum termina: ${selectedDateTime.format(formatter)}"
        textViewSelectedDate.setTextColor(resources.getColor(android.R.color.black, theme))
    }
}