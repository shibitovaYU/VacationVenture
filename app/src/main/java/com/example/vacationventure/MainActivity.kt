package com.example.vacationventure

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vacationventure.data.cityCodeMapTickets
import com.example.vacationventure.data.cityMap
import com.example.vacationventure.data.cityNames
import com.example.vacationventure.data.cityTranslations
import com.example.vacationventure.model.FlightSearchResponse
import com.example.vacationventure.model.HotelSearchResponse
import com.example.vacationventure.model.TrainSearchResponse
import com.example.vacationventure.models.Event
import com.example.vacationventure.network.YandexRaspService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.io.IOException
import java.net.URLEncoder
import java.security.cert.X509Certificate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.thread
import kotlin.concurrent.timer


private lateinit var retrofit: Retrofit

private const val TAG = "FlightSearch"

class MainActivity : AppCompatActivity() {
    data class Segment(
        val departure: Departure,
        val arrival: Arrival,
        val carrierCode: String,
        val number: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(Departure::class.java.classLoader) ?: Departure("", ""),
            parcel.readParcelable(Arrival::class.java.classLoader) ?: Arrival("", ""),
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(departure, flags)
            parcel.writeParcelable(arrival, flags)
            parcel.writeString(carrierCode)
            parcel.writeString(number)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Segment> {
            override fun createFromParcel(parcel: Parcel): Segment {
                return Segment(parcel)
            }
            override fun newArray(size: Int): Array<Segment?> {
                return arrayOfNulls(size)
            }
        }
    }
    data class Departure(
        val iataCode: String,
        val at: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(iataCode)
            parcel.writeString(at)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Departure> {
            override fun createFromParcel(parcel: Parcel): Departure {
                return Departure(parcel)
            }

            override fun newArray(size: Int): Array<Departure?> {
                return arrayOfNulls(size)
            }
        }
    }
    data class Arrival(
        val iataCode: String,
        val at: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(iataCode)
            parcel.writeString(at)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Arrival> {
            override fun createFromParcel(parcel: Parcel): Arrival {
                return Arrival(parcel)
            }

            override fun newArray(size: Int): Array<Arrival?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Contact(
        val phone: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: ""
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(phone)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Contact> {
            override fun createFromParcel(parcel: Parcel): Contact {
                return Contact(parcel)
            }
            override fun newArray(size: Int): Array<Contact?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Offer(
        val price: Price
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(Price::class.java.classLoader) ?: Price(0.0)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(price, flags)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Offer> {
            override fun createFromParcel(parcel: Parcel): Offer {
                return Offer(parcel)
            }

            override fun newArray(size: Int): Array<Offer?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Price(
        val total: Double
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readDouble()
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeDouble(total)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Price> {
            override fun createFromParcel(parcel: Parcel): Price {
                return Price(parcel)
            }

            override fun newArray(size: Int): Array<Price?> {
                return arrayOfNulls(size)
            }
        }
    }
    data class EventResponse(
        val _embedded: EmbeddedEvents? // Убедитесь, что используется `EmbeddedEvents?` для обработки возможных null-значений
    )

    data class EmbeddedEvents(
        val events: List<Event> // Список событий
    )
    data class EmbeddedVenues(
        val venues: List<Venue>
    )

    data class Venue(
        val name: String
    )

    data class Media(
        val uri: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: ""
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(uri)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Media> {
            override fun createFromParcel(parcel: Parcel): Media {
                return Media(parcel)
            }

            override fun newArray(size: Int): Array<Media?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class RestaurantSearchResponse(
        val status: Boolean,
        val message: String,
        val data: RestaurantData
    )
    data class RestaurantData(
        val totalRecords: Int,
        val totalPages: Int,
        val data: List<Restaurant>
    )
    data class Photo(
        val url: String,
        val caption: String?
    )
    data class Location(
        val locationId: String, // ID локации
        val name: String // Название локации
    )
    data class Geo(
        val geoId: String, // ID локации
        val name: String // Название локации
    )
    data class Category(
        val name: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<Category> {
            override fun createFromParcel(parcel: Parcel): Category {
                return Category(parcel)
            }

            override fun newArray(size: Int): Array<Category?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class LocationResponse(
        val data: List<Location>
    )
    data class GeoResponse(
        val data: List<Geo>
    )

    interface TicketmasterApi {
        @GET("/discovery/v2/events.json")
        fun searchEvents(
            @Query("apikey") apiKey: String,
            @Query("city") city: String,
            @Query("date") date: String
        ): Call<EventResponse>
    }

    interface FoursquareApiService {
        @GET("v3/places/search")
        fun searchRestaurants(
            @Header("Authorization") authToken: String,
            @Query("near") city: String,  // Поиск по городу
            @Query("categories") categories: String,  // Поиск по типу кухни
            @Query("limit") limit: Int = 1000           // Лимит результатов (например, 20 ресторанов)
        ): Call<RestaurantSearchResponse>
    }

    private lateinit var retrofitSecond: Retrofit
    private lateinit var apiServiceMaster: TicketmasterApi
    private lateinit var foursquareApiService: FoursquareApiService

    private var selectedButton: ImageButton? = null
    private lateinit var flightTrainLayout: LinearLayout
    private lateinit var hotelLayout: LinearLayout
    private lateinit var restaurantEventLayout: LinearLayout
    private lateinit var entertainmentLayout: LinearLayout

    private lateinit var departureDateEditText: EditText
    private lateinit var departureLocationEditText: AutoCompleteTextView
    private lateinit var arrivalLocationEditText: AutoCompleteTextView
    private lateinit var inputPassengersEditText: EditText

    private lateinit var cuisineSpinner: Spinner
    private lateinit var ageCategorySpinner: Spinner
    private lateinit var eventDateEditText: EditText
    private lateinit var inputCheckOutDateEditText: EditText
    private lateinit var inputCheckInDateEditText: EditText
    private lateinit var inputDateEntertainmentEditText: EditText
    private lateinit var inputCityEditText: EditText
    private lateinit var inputVisitorsEditText: EditText
    private lateinit var tripAdvisorApiService: TripAdvisorApiService
    private lateinit var inputCityRestaurantEventEditText: EditText
    private lateinit var spinnerCuisine: Spinner
    private lateinit var eventCityEditText:EditText
    private lateinit var noResultsView: LinearLayout
    private lateinit var noResultsTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var articleManager: ArticleManager
    private lateinit var yandexRaspService: YandexRaspService
    private var articleIndex: Int = 0
    private var transportType: String = "plane"
    private val apiKey = "GOar6YtGHaAjqNpicHckvzO4CPtio3LQ"
    private val tag = "TicketSearch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale("ru")
        setContentView(R.layout.activity_main)

        departureDateEditText = findViewById(R.id.input_departure_date)
        departureLocationEditText = findViewById(R.id.input_departure)
        arrivalLocationEditText = findViewById(R.id.input_arrival)
        inputCheckOutDateEditText = findViewById(R.id.input_checkout_date)
        inputCityEditText = findViewById(R.id.input_city)
        inputVisitorsEditText = findViewById(R.id.input_visitors)
        inputCheckInDateEditText = findViewById(R.id.input_checkin_date)
        inputDateEntertainmentEditText = findViewById(R.id.input_date_entertainment)
        inputCityRestaurantEventEditText = findViewById(R.id.input_city_restaurant_event)

        departureDateEditText.setOnClickListener {
            showDatePicker(departureDateEditText)
        }

        inputCheckOutDateEditText.setOnClickListener {
            showDatePicker(inputCheckOutDateEditText)
        }
        inputCheckInDateEditText.setOnClickListener {
            showDatePicker(inputCheckInDateEditText)
        }
        inputDateEntertainmentEditText.setOnClickListener {
            showDatePicker(inputDateEntertainmentEditText)
        }

        flightTrainLayout = findViewById(R.id.flight_train_layout)
        hotelLayout = findViewById(R.id.hotel_layout)
        restaurantEventLayout = findViewById(R.id.restaurant_event_layout)
        entertainmentLayout = findViewById(R.id.entertainment_layout)
        eventDateEditText = findViewById(R.id.input_date_entertainment)
        eventCityEditText = findViewById(R.id.input_city_entertainment)
        messageTextView = findViewById(R.id.message_text_view)

        eventDateEditText.setOnClickListener {
            showDatePicker(eventDateEditText)
        }

        articleManager = ArticleManager()

        firebaseAuth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("user_preferences")

        // Показываем всплывающее окно с предложением статьи при запуске
        loadUserPreferences()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.rasp.yandex-net.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        yandexRaspService = retrofit.create(YandexRaspService::class.java)

        val buttonFlight = findViewById<ImageButton>(R.id.button_flight)
        val buttonTrain = findViewById<ImageButton>(R.id.button_train)
        val buttonHome = findViewById<ImageButton>(R.id.button_home)
        val buttonFood = findViewById<ImageButton>(R.id.button_food)
        val buttonEnter = findViewById<ImageButton>(R.id.button_enter)
        val buttonFind = findViewById<Button>(R.id.main_button)

        val favoritesButton: ImageButton = findViewById(R.id.button_favorites)
        val mainButton: ImageButton = findViewById(R.id.button_main)
        val profileButton: ImageButton = findViewById(R.id.button_profile)
        val buttonHotel = findViewById<ImageButton>(R.id.button_home)

        setupAutoCompleteForCity(findViewById(R.id.input_city_restaurant_event))
        setupAutoCompleteForCity(findViewById(R.id.input_city))

        favoritesButton.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val inputVisitorsEditText = findViewById<EditText>(R.id.input_visitors)
        inputVisitorsEditText.setOnClickListener {
            showPassengerPickerDialog(inputVisitorsEditText)
        }
        selectButton(buttonFlight)
        updateUIForSection("Flight")

        buttonFlight.setOnClickListener {
            selectButton(buttonFlight)
            updateUIForSection("Flight")
            transportType = "plane"
        }
        buttonTrain.setOnClickListener {
            selectButton(buttonTrain)
            updateUIForSection("Train")
            transportType = "train"
        }
        buttonHome.setOnClickListener {
            selectButton(buttonHome)
            updateUIForSection("Hotel")
        }
        buttonFood.setOnClickListener {
            selectButton(buttonFood)
            updateUIForSection("Restaurant")
        }
        buttonEnter.setOnClickListener {
            selectButton(buttonEnter)
            updateUIForSection("Event")
        }

        retrofitSecond = Retrofit.Builder()
            .baseUrl("https://tripadvisor16.p.rapidapi.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val tripAdvisorApiService = retrofit.create(TripAdvisorApiService::class.java)

        val selectedTab = intent.getStringExtra("selected_tab")
        if (selectedTab == "Restaurant") {
            selectButton(buttonFood)
            updateUIForSection("Restaurant")
        }
        if (selectedTab == "plane") {
            selectButton(buttonFlight)
            updateUIForSection("Flight")
        }
        if (selectedTab == "train") {
            selectButton(buttonTrain)
            updateUIForSection("Train")
        }

        // Разрешаем выполнение сетевых запросов в основном потоке (для примера)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val cityInput: AutoCompleteTextView = findViewById(R.id.input_city_entertainment)

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNames)
        cityInput.setAdapter(adapter)

        val retrofitTicketmaster = Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/") // URL для Ticketmaster API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiServiceMaster = retrofitTicketmaster.create(TicketmasterApi::class.java)
        val cityNames = cityCodeMapTickets.keys.toList()

        val departureLocationEditText: AutoCompleteTextView = findViewById(R.id.input_departure)
        val arrivalLocationEditText: AutoCompleteTextView = findViewById(R.id.input_arrival)

        val adapterTickets = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNames)
        departureLocationEditText.setAdapter(adapterTickets)
        arrivalLocationEditText.setAdapter(adapterTickets)

        var departureCityCode = ""
        var arrivalCityCode = ""

        // Получение кода города при выборе элемента
        departureLocationEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position) as String
            departureCityCode = cityCodeMapTickets[selectedCity] ?: ""
        }

        arrivalLocationEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position) as String
            arrivalCityCode = cityCodeMapTickets[selectedCity] ?: ""
        }

        buttonFind.setOnClickListener {
            val city = inputCityRestaurantEventEditText.text.toString().trim()
            val cityHotel = inputCityEditText.text.toString().trim()
            val translatedCityHotel = cityTranslations[cityHotel] ?: cityHotel
            val translatedCityFood = cityTranslations[city] ?: city
            val cityEvent = eventCityEditText.text.toString().trim()
            val date = eventDateEditText.text.toString().trim()
            val departure = departureCityCode
            val arrival = arrivalCityCode
            val departureDate = departureDateEditText.text.toString()
            Log.d("MainActivity", "Кнопка поиска нажата") // Логирование нажатия на кнопку
            when (selectedButton) {
                buttonFlight -> searchFlight(departure, arrival,departureDate)
                buttonTrain -> searchTrain(departure, arrival, departureDate)
                buttonHotel -> searchGeo(translatedCityHotel, "hotel")
                buttonFood -> searchLocation(translatedCityFood, "restaurant")
                buttonEnter -> searchEvents(cityEvent,date)

        }

        }
}

private fun selectButton(button: ImageButton) {
        selectedButton?.isSelected = false
        selectedButton?.setBackgroundResource(R.drawable.button_selector)

        button.isSelected = true
        button.setBackgroundResource(R.drawable.button_selector)

        selectedButton = button
    }
    private fun updateUIForSection(section: String) {
        clearInputFields()

        flightTrainLayout.visibility = LinearLayout.GONE
        hotelLayout.visibility = LinearLayout.GONE
        restaurantEventLayout.visibility = LinearLayout.GONE
        entertainmentLayout.visibility = LinearLayout.GONE

        val mainButton = findViewById<Button>(R.id.main_button)

        when (section) {
            "Flight", "Train" -> {
                flightTrainLayout.visibility = LinearLayout.VISIBLE
                mainButton.text = if (section == "Flight") "Найти билет" else "Найти поезд"
            }
            "Hotel" -> {
                hotelLayout.visibility = LinearLayout.VISIBLE
                mainButton.text = "Найти отель"
            }
            "Restaurant" -> {
                restaurantEventLayout.visibility = LinearLayout.VISIBLE
                mainButton.text = "Найти ресторан"
            }
            "Event" -> {
                entertainmentLayout.visibility = LinearLayout.VISIBLE
                mainButton.text = "Найти мероприятие"
            }
        }
    }
    private fun clearInputFields() {
        if (flightTrainLayout.visibility == LinearLayout.VISIBLE) {
            departureDateEditText.text.clear()
            departureLocationEditText.text.clear()
            arrivalLocationEditText.text.clear()
            eventDateEditText.text.clear()
        }
        if (hotelLayout.visibility == LinearLayout.VISIBLE) {
            inputCheckInDateEditText.text.clear()
            inputCheckOutDateEditText.text.clear()
            inputCityEditText.text.clear()
            inputVisitorsEditText.text.clear()
        }
    }
    private fun showPassengerPickerDialog(editText: EditText) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_passenger_picker, null)
        val numberPickerAdults = dialogView.findViewById<NumberPicker>(R.id.numberPickerAdults)
        val numberPickerChildren = dialogView.findViewById<NumberPicker>(R.id.numberPickerChildren)

        numberPickerAdults.minValue = 1
        numberPickerAdults.maxValue = 10
        numberPickerChildren.minValue = 0
        numberPickerChildren.maxValue = 5

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Выберите количество пассажиров")
            .setPositiveButton("ОК") { _, _ ->
                val adults = numberPickerAdults.value
                val children = numberPickerChildren.value
                editText.setText("Взрослых: $adults, Детей: $children")
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePicker,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Замените здесь на форматирование даты
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                editText.setText(selectedDate) // Устанавливаем отформатированную дату
            },
            year, month, day
        )

        val locale = Locale("ru")
        Locale.setDefault(locale)
        datePickerDialog.show()
    }
    interface TripAdvisorApiService {
        @GET("/api/v1/location/searchRestaurants")
        fun searchLocation(
            @Query("query") city: String,
            @Header("x-rapidapi-host") host: String = "tripadvisor16.p.rapidapi.com",
            @Header("x-rapidapi-key") apiKey: String = "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c" // Ваш ключ API
        ): Call<LocationResponse>

        @GET("/api/v1/hotels/searchHotels")
        fun searchGeo(
            @Query("query") cityHotel: String,
            @Header("x-rapidapi-host") host: String = "tripadvisor16.p.rapidapi.com",
            @Header("x-rapidapi-key") apiKey: String = "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c" // Ваш ключ API
        ): Call<GeoResponse>

        @GET("/api/v1/restaurant/searchRestaurants")
        fun searchRestaurants(
            @Query("locationId") locationId: String,
            @Header("x-rapidapi-host") host: String = "tripadvisor16.p.rapidapi.com",
            @Header("x-rapidapi-key") apiKey: String = "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c" // Ваш ключ API
        ): Call<RestaurantSearchResponse>

        @GET("/api/v1/hotels/searchHotels")
        fun searchHotels(
            @Query("geoId") geoId: String,
            @Header("x-rapidapi-host") host: String = "tripadvisor16.p.rapidapi.com",
            @Header("x-rapidapi-key") apiKey: String = "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c" // Ваш ключ API
        ): Call<HotelSearchResponse>
    }
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    val geoIds: List<String> = listOf()
    private fun extractVisitorCounts(visitorText: String): Pair<Int, Int> {
        val adultsRegex = Regex("Взрослых: (\\d+)")
        val childrenRegex = Regex("Детей: (\\d+)")

        val adultsMatch = adultsRegex.find(visitorText)
        val childrenMatch = childrenRegex.find(visitorText)

        val adults = adultsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1 // По умолчанию 1 взрослый
        val children = childrenMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0 // По умолчанию 0 детей

        return Pair(adults, children)
    }
    private fun setupAutoCompleteForCity(autoCompleteTextView: AutoCompleteTextView) {
        val cities = cityTranslations.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 1
    }
    private fun searchLocation(city: String, searchType: String) {
        val client = OkHttpClient()
        val encodedCity = URLEncoder.encode(city, "UTF-8") // Кодируем строку запроса
        Log.d("MainActivity", "Encoded city: $encodedCity")

        val url = "https://tripadvisor16.p.rapidapi.com/api/v1/restaurant/searchLocation?query=$encodedCity"

        val requestTrip = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c") // Замените на ваш API ключ
            .addHeader("x-rapidapi-host", "tripadvisor16.p.rapidapi.com")
            .build()

        thread {
            try {
                val response: okhttp3.Response = client.newCall(requestTrip).execute()
                Log.d("MainActivity", "Response code: ${response.code}") // Логируем код ответа
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("MainActivity", "Response data: $responseData") // Логируем ответ
                    val locationId = parseLocationId(responseData)
                    Log.d("MainActivity", "Parsed location ID: $locationId")
                    if (locationId != null) {
                        if (searchType == "restaurant") {
                            searchRestaurants(locationId)
                        }
                    } else {
                        // Отображаем ошибку через Toast
                        runOnUiThread {
                            Toast.makeText(this, "Локация не найдена для города: $city", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("MainActivity", "Ошибка поиска локации: ${response.code} - ${response.message}")
                    // Отображаем ошибку через Toast
                    runOnUiThread {
                        Toast.makeText(this, "Ошибка поиска локации: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Отображаем ошибку сети через Toast
                runOnUiThread {
                    Toast.makeText(this, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun searchGeo(cityHotel: String, searchType: String) {
        val client = OkHttpClient()
        val encodedCity = cityHotel // Кодируем строку запроса
        Log.d("MainActivity", "Encoded city: $encodedCity")

        val url = "https://tripadvisor16.p.rapidapi.com/api/v1/hotels/searchLocation?query=$encodedCity"

        val requestTrip = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c") // Замените на ваш API ключ
            .addHeader("x-rapidapi-host", "tripadvisor16.p.rapidapi.com")
            .build()

        thread {
            try {
                val response: okhttp3.Response = client.newCall(requestTrip).execute()
                Log.d("MainActivity", "Response code: ${response.code}") // Логируем код ответа
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("MainActivity", "Response data: $responseData") // Логируем ответ
                    val geoId = parseGeoId(responseData)
                    Log.d("MainActivity", "Parsed geo ID: $geoId")
                    if (geoId != null) {
                        searchHotels(geoId)
                    } else {
                        // Отображаем ошибку через Toast
                        runOnUiThread {
                            Toast.makeText(this, "Локация не найдена для города: $cityHotel", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("MainActivity", "Ошибка поиска локации: ${response.code} - ${response.message}")
                    // Отображаем ошибку через Toast
                    runOnUiThread {
                        Toast.makeText(this, "Ошибка поиска локации: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Отображаем ошибку сети через Toast
                runOnUiThread {
                    Toast.makeText(this, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun searchRestaurants(locationId: String?) {
        // Проверка на заполнение поля locationId
        if (locationId.isNullOrEmpty()) {
            Log.e("searchRestaurants", "Location ID is empty")
            Toast.makeText(this, "Пожалуйста, укажите идентификатор местоположения!", Toast.LENGTH_SHORT).show()
            return
        }

        // Таймер для отмены запроса, если он занимает слишком много времени (например, 30 секунд)
        val timeout = 30_000L // 30 секунд
        var isRequestCompleted = false

        // Создаем клиент и запрос
        val client = OkHttpClient()
        val requestSearch = Request.Builder()
            .url("https://tripadvisor16.p.rapidapi.com/api/v1/restaurant/searchRestaurants?locationId=$locationId")
            .get()
            .addHeader("x-rapidapi-key", "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c") // Ваш API ключ
            .addHeader("x-rapidapi-host", "tripadvisor16.p.rapidapi.com")
            .build()

        // Таймер отмены запроса
        timer(name = "timeout", initialDelay = timeout, period = timeout) {
            if (!isRequestCompleted) {
                client.dispatcher.executorService.shutdownNow() // Останавливаем запрос
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Превышено время ожидания!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Отправляем запрос в отдельном потоке
        thread {
            try {
                val response: okhttp3.Response = client.newCall(requestSearch).execute()
                isRequestCompleted = true

                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("searchRestaurants", "Response received: $responseData")

                    val restaurants = parseRestaurants(responseData) // Метод для парсинга данных о ресторанах

                    // Если рестораны не найдены
                    if (restaurants.isNullOrEmpty()) {
                        runOnUiThread {
                            Log.e("searchRestaurants", "No restaurants found.")
                            Toast.makeText(this@MainActivity, "Рестораны не найдены для данного местоположения.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Отображаем найденные рестораны
                        runOnUiThread {
                            showRestaurants(restaurants) // Метод для отображения ресторанов в UI
                        }
                    }
                } else {
                    // Ошибка при выполнении запроса
                    runOnUiThread {
                        Log.e("searchRestaurants", "Error in response: ${response.message}")
                        Toast.makeText(this@MainActivity, "Ошибка поиска ресторанов: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                isRequestCompleted = true
                Log.e("searchRestaurants", "Network error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun parseLocationId(responseData: String?): String? {
        if (responseData.isNullOrEmpty()) return null
        val gson = Gson()
        val locationResponse = gson.fromJson(responseData, LocationResponse::class.java)
        return locationResponse.data.firstOrNull()?.locationId
    }
    private fun parseGeoId(responseData: String?): String? {
        if (responseData.isNullOrEmpty()) return null
        val gson = Gson()
        val geoResponse = gson.fromJson(responseData, GeoResponse::class.java)
        return geoResponse.data.firstOrNull()?.geoId
    }
    private fun parseRestaurants(responseData: String?): List<Restaurant> {
        if (responseData.isNullOrEmpty()) return emptyList() // Возвращаем пустой список, если данных нет

        val gson = Gson()
        val restaurantResponse = gson.fromJson(responseData, RestaurantSearchResponse::class.java)

        return restaurantResponse.data.data.map {
            Log.d("MainActivity", "Restaurant data: Name: ${it.name}, ID: ${it.restaurantsId}, Rating: ${it.averageRating}, userReviewCount: ${it.userReviewCount}, price: ${it.priceTag}")
            Restaurant(
                name = it.name,
                restaurantsId = it.restaurantsId,
                averageRating = it.averageRating,            // Средний рейтинг
                userReviewCount = it.userReviewCount,        // Количество отзывов
                currentOpenStatusText = it.currentOpenStatusText, // Статус ресторана
                priceTag = it.priceTag,                      // Уровень цен
                menuUrl = it.menuUrl,                        // URL меню (если есть)
                heroImgUrl = it.heroImgUrl                // Главное изображение (если есть)
            )
        }
    }
    private fun showRestaurants(restaurants: List<Restaurant>) {
        val intent = Intent(this, RestaurantListActivity::class.java)
        intent.putParcelableArrayListExtra("restaurant_list", ArrayList(restaurants))
        startActivity(intent)
    }

    private fun searchHotels(geoId: String) {
        // Извлекаем дату заезда из EditText
        val checkInText = inputCheckInDateEditText.text.toString().trim()
        val checkOutText = inputCheckOutDateEditText.text.toString().trim()

        // Проверяем, что текст не пустой
        if (checkInText.isEmpty() || checkOutText.isEmpty()) {
            showErrorDialog("Пожалуйста, введите дату заезда и выезда")
            return
        }

        // Пытаемся распарсить введенные даты
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val checkInDate: Date
        val checkOutDate: Date

        try {
            checkInDate = dateFormat.parse(checkInText) ?: throw Exception("Не удалось распарсить дату заезда")
            checkOutDate = dateFormat.parse(checkOutText) ?: throw Exception("Не удалось распарсить дату выезда")
        } catch (e: ParseException) {
            showErrorDialog("Неверный формат даты. Пожалуйста, используйте формат yyyy-MM-dd.")
            return
        }

        // Проверяем, что даты в будущем
        val currentDate = Date()
        if (checkInDate.before(currentDate) || checkOutDate.before(currentDate)) {
            showErrorDialog("Даты не могут быть в прошлом.")
            return
        }

        // Проверяем, что дата выезда позже даты заезда
        if (checkOutDate.before(checkInDate)) {
            showErrorDialog("Дата выезда должна быть позже даты заезда.")
            return
        }

        // Форматируем даты для запроса
        val formattedCheckIn = dateFormat.format(checkInDate)
        val formattedCheckOut = dateFormat.format(checkOutDate)

        Log.d("MainActivity", "CheckIn: $formattedCheckIn, CheckOut: $formattedCheckOut")

        // Создаем клиент с таймаутами
        val client = createHttpClient()

        val requestSearch = Request.Builder()
            .url("https://tripadvisor16.p.rapidapi.com/api/v1/hotels/searchHotels?geoId=$geoId&checkIn=$formattedCheckIn&checkOut=$formattedCheckOut")
            .get()
            .addHeader("x-rapidapi-key", "c629611588msh1e8deae2c3f5133p1e3c9bjsn6797017ed79c") // Замените на ваш API ключ
            .addHeader("x-rapidapi-host", "tripadvisor16.p.rapidapi.com")
            .build()

        // Асинхронный запрос
        thread {
            try {
                val response: okhttp3.Response = client.newCall(requestSearch).execute()
                if (response.isSuccessful) {
                    // Получаем тело ответа
                    val responseData = response.body?.string()

                    // Проверяем, что responseData не null
                    if (responseData != null) {
                        val jsonObject = JSONObject(responseData) // Преобразуем строку JSON в JSONObject
                        val hotelsArray = jsonObject.getJSONObject("data").getJSONArray("data") // Извлекаем JSONArray
                        val hotels = parseHotels(hotelsArray) // Передаем JSONArray в parseHotels

                        showHotels(hotels) // Отображение отелей
                    } else {
                        Log.e("MainActivity", "Ошибка: ответ пустой")
                        runOnUiThread { showErrorDialogHotels(this,"Ошибка: ответ пустой") }
                    }
                } else {
                    Log.e("MainActivity", "Ошибка поиска отелей: ${response.code} - ${response.message}")
                    runOnUiThread { showErrorDialogHotels(this,"Ошибка поиска отелей: ${response.message}") }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread { showErrorDialogHotels(this,"Ошибка сети: ${e.message}") }
            }
        }
    }

    // Создаем OkHttpClient с таймаутами
    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Таймаут для установления соединения
            .readTimeout(30, TimeUnit.SECONDS)     // Таймаут для чтения данных
            .writeTimeout(30, TimeUnit.SECONDS)    // Таймаут для записи данных
            .retryOnConnectionFailure(true)        // Повторять попытки при неудачных соединениях
            .build()
    }


    private fun parseHotels(hotelsJson: JSONArray): List<Hotel> {
        return (0 until hotelsJson.length()).mapNotNull { index ->
            val hotelJson = hotelsJson.optJSONObject(index) ?: return@mapNotNull null

            val id = hotelJson.optString("id")
            val title = hotelJson.optString("title", "Неизвестное название")
            val primaryInfo = hotelJson.optString("primaryInfo", "Нет описания отеля")
            val secondaryInfo = hotelJson.optString("secondaryInfo", "Нет дополнительной информации")

            val bubbleRatingJson = hotelJson.optJSONObject("bubbleRating")
            val bubbleRating = parseBubbleRating(bubbleRatingJson)

            val count = bubbleRating.count.replace(",", "").toIntOrNull() ?: 0 // Используем count из bubbleRating

            val priceForDisplay = hotelJson.optString("priceForDisplay", "Цена не указана")
            val priceSummary = hotelJson.optString("priceSummary", "Нет информации о ценах")

            val cardPhotosJson = hotelJson.optJSONArray("cardPhotos")
            val cardPhotos = parseCardPhotos(cardPhotosJson)
            val firstImage = cardPhotos.firstOrNull() // Берём первую картинку

            val commerceInfoJson = hotelJson.optJSONObject("commerceInfo")
            val tripAdvisorUrl = commerceInfoJson?.optString("externalUrl")

            Log.d("HotelParser", "Hotel ID: $id, Image URL: $firstImage, Price: $priceForDisplay")

            if (id != null) {
                Hotel(
                    id = id,
                    title = title,
                    primaryInfo = primaryInfo,
                    secondaryInfo = secondaryInfo,
                    bubbleRating = bubbleRating,
                    count = count,
                    priceForDisplay = "$priceForDisplay - $priceSummary",
                    cardPhotos = if (firstImage != null) listOf(firstImage) else emptyList(),
                    tripAdvisorUrl = tripAdvisorUrl
                )
            } else {
                null
            }
        }
    }
    private fun showHotels(hotels: List<Hotel>) {
        if (hotels.isEmpty()) {
            runOnUiThread { showErrorDialogHotels(this, "Отели не найдены.") }
        } else {
            val intent = Intent(this, HotelListActivity::class.java)
            intent.putParcelableArrayListExtra("hotel_list", ArrayList(hotels))
            startActivity(intent)
        }
    }

    private fun parseBubbleRating(bubbleJson: JSONObject?): BubbleRating {
        if (bubbleJson == null) return BubbleRating("0", 0.0)

        val count = bubbleJson.optString("count", "0")
        val rating = bubbleJson.optDouble("rating", 0.0)

        return BubbleRating(count, rating)
    }
    private fun parseCardPhotos(cardPhotosArray: JSONArray?): List<String> {
        if (cardPhotosArray == null) return emptyList()

        return List(cardPhotosArray.length()) { index ->
            cardPhotosArray.getJSONObject(index)
                .optJSONObject("sizes")
                ?.optString("urlTemplate") // Получаем urlTemplate
        }.filterNotNull() // Исключаем null значения
    }

    private fun getUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    private fun searchEvents(city: String, date: String) {
        // Проверка на заполнение полей
        if (city.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля!", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка даты на прошлое
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val inputDate = try {
            dateFormat.parse(date)
        } catch (e: Exception) {
            null
        }

        val currentDate = Date()

        if (inputDate == null || inputDate.before(currentDate)) {
            Toast.makeText(this, "Дата не может быть в прошлом!", Toast.LENGTH_SHORT).show()
            return
        }

        val cityInfo = cityMap[city] // Получаем информацию о городе из карты
        val englishCity = cityInfo?.first ?: city // Используем английское название или оригинал
        val countryCode = cityInfo?.second ?: "RU" // Используем код страны, если он доступен

        val apiKey = "GOar6YtGHaAjqNpicHckvzO4CPtio3LQ" // Ваш API ключ

        val call = apiServiceMaster.searchEvents(apiKey, englishCity, date)

        // Таймер для отмены запроса, если он занимает слишком много времени (например, 30 секунд)
        val timeout = 30_000L // 30 секунд
        var isRequestCompleted = false

        // Таймер отмены запроса
        timer(name = "timeout", initialDelay = timeout, period = timeout) {
            if (!isRequestCompleted) {
                call.cancel() // Отменяем запрос, если он не завершился за 30 секунд
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Превышено время ожидания!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        call.enqueue(object : Callback<EventResponse> {
            override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                // Остановим таймер, если запрос завершился
                isRequestCompleted = true

                if (response.isSuccessful) {
                    val events = response.body()?._embedded?.events

                    // Удаляем лишние мероприятия (например, с пустыми полями или уже прошедшие события)
                    val validEvents = events?.filter { event ->
                        // Фильтруем события, оставляем только актуальные
                        val eventDate = try {
                            dateFormat.parse(event?.dates?.start?.localDate)
                        } catch (e: Exception) {
                            null
                        }
                        eventDate != null && !eventDate.before(currentDate) // Событие не должно быть в прошлом
                    }

                    // Если есть актуальные события
                    if (validEvents != null && validEvents.isNotEmpty()) {
                        val intent = Intent(this@MainActivity, EventResultsActivity::class.java)
                        intent.putParcelableArrayListExtra("events", ArrayList(validEvents))
                        intent.putExtra("selected_date", date) // Передаем выбранную дату
                        startActivity(intent)
                    } else {
                        Log.d("MainActivity", "No valid events found")
                        Toast.makeText(this@MainActivity, "Нет доступных мероприятий!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Event", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MainActivity, "Ошибка при загрузке данных. Попробуйте позже.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                isRequestCompleted = true
                Log.e("Event", "Failure: ${t.message}")
                Toast.makeText(this@MainActivity, "Не удалось получить данные. Попробуйте позже.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun showErrorDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("ОК", null)
                .show()
        }
    }
    private fun loadUserPreferences() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        Log.d("MainActivity", "Загрузка предпочтений для пользователя с ID: $userId")

        database.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userPreferences = snapshot.getValue(UserPreferences::class.java)
                userPreferences?.let { preferences ->
                    Log.d("MainActivity", "Получены предпочтения пользователя: $preferences")

                    // Фильтрация статей на основе предпочтений пользователя
                    val filteredArticles = articleManager.getFilteredArticles(preferences)

                    if (filteredArticles.isNotEmpty()) {
                        articleIndex = preferences.articleIndex % filteredArticles.size
                        showSuggestedArticle(filteredArticles)
                    } else {
                        Log.d("MainActivity", "Нет статей, соответствующих предпочтениям пользователя")
                    }
                }
            } else {
                Log.d("MainActivity", "Предпочтения пользователя не найдены")
            }
        }.addOnFailureListener { exception ->
            Log.e("MainActivity", "Ошибка загрузки предпочтений: ${exception.message}")
        }
    }
    private fun showSuggestedArticle(filteredArticles: List<VacationArticle>) {
        val nextArticle = articleManager.getArticleByIndex(articleIndex)

        // Настройка пользовательского диалога
        val dialogView = layoutInflater.inflate(R.layout.dialog_suggested_article, null)
        val articleImage = dialogView.findViewById<ImageView>(R.id.articleImage)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnYes).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(nextArticle.url))
            startActivity(browserIntent)
            saveNextArticleIndex()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnNoThanks).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun saveNextArticleIndex() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        articleIndex = (articleIndex + 1) % articleManager.getArticlesCount()
        database.child(userId).child("articleIndex").setValue(articleIndex)
    }

    private fun searchFlight(departure: String, arrival: String, date: String) {
        // Проверка на пустые поля
        if (departure.isBlank() || arrival.isBlank() || date.isBlank()) {
            Toast.makeText(this@MainActivity, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка на правильность даты
        if (!isDateInFuture(date)) {
            Toast.makeText(this@MainActivity, "Выберите корректную дату, она не может быть в прошлом", Toast.LENGTH_SHORT).show()
            return
        }

        val apiKey = "c10f36f2-916b-47b7-891d-8a0806dac6e6"
        val call = yandexRaspService.searchFlight(departure, arrival, apiKey, date)

        // Создаем таймер, который сработает через 5 секунд, если запрос не завершится
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Toast.makeText(this@MainActivity, "Извините, мы не можем предоставить результат", Toast.LENGTH_SHORT).show()
        }
        handler.postDelayed(timeoutRunnable, 5000)

        call.enqueue(object : Callback<FlightSearchResponse> {
            override fun onResponse(
                call: Call<FlightSearchResponse>,
                response: Response<FlightSearchResponse>
            ) {
                handler.removeCallbacks(timeoutRunnable)

                if (response.isSuccessful) {
                    val flightResponse = response.body()
                    if (flightResponse != null) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        Log.d(TAG, "JSON response:\n${gson.toJson(flightResponse)}")

                        handleFlightResponse(flightResponse)
                    } else {
                        Log.w(TAG, "Response body is null")
                        Toast.makeText(this@MainActivity, "Ответ пуст", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Важно: errorBody().string() можно прочитать только 1 раз
                    val err = response.errorBody()?.string()
                    Log.e(TAG, "HTTP ${response.code()} ${response.message()} | errorBody: $err")
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка: ${response.code()} - ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FlightSearchResponse>, t: Throwable) {
                handler.removeCallbacks(timeoutRunnable)
                Log.e(TAG, "Request failed", t)
            }
        })
    }
    private fun searchTrain(departure: String, arrival: String, date: String) {
        // Проверка на пустые поля
        if (departure.isBlank() || arrival.isBlank() || date.isBlank()) {
            Toast.makeText(this@MainActivity, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка на правильность даты
        if (!isDateInFuture(date)) {
            Toast.makeText(this@MainActivity, "Выберите корректную дату, она не может быть в прошлом", Toast.LENGTH_SHORT).show()
            return
        }

        val apiKey = "c10f36f2-916b-47b7-891d-8a0806dac6e6"
        val call = yandexRaspService.searchTrain(departure, arrival, apiKey, date)

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Toast.makeText(this@MainActivity, "Извините, мы не можем предоставить результат", Toast.LENGTH_SHORT).show()
        }
        handler.postDelayed(timeoutRunnable, 60000)

        call.enqueue(object : Callback<TrainSearchResponse> {
            override fun onResponse(call: Call<TrainSearchResponse>, response: Response<TrainSearchResponse>) {
                handler.removeCallbacks(timeoutRunnable)  // Останавливаем таймер
                if (response.isSuccessful) {
                    response.body()?.let { trainResponse ->
                        handleTrainResponse(trainResponse)
                    }
                } else {
                    Log.e("TrainSearch", "Ошибка: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TrainSearchResponse>, t: Throwable) {
                handler.removeCallbacks(timeoutRunnable)  // Останавливаем таймер
                Log.e("TrainSearch", "Ошибка подключения: ${t.localizedMessage}")
            }
        })
    }
    private fun isDateInFuture(date: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val inputDate = sdf.parse(date) ?: return false
        val currentDate = Calendar.getInstance().time
        return !inputDate.before(currentDate)
    }
    val cityCodeMapTicketsCheck = listOf(
        "Санкт-Петербург", "Белгород", "Иваново", "Калуга", "Кострома", "Курск", "Липецк", "Орел", "Рязань", "Смоленск", "Тамбов", "Тверь", "Тула", "Ярославль", "Петрозаводск", "Сыктывкар", "Архангельск", "Вологда", "Калининград", "Мурманск", "Великий Новгород", "Псков", "Махачкала", "Нальчик", "Владикавказ", "Краснодар", "Ставрополь", "Астрахань", "Волгоград", "Ростов-на-Дону", "Йошкар-Ола", "Саранск", "Казань", "Ижевск", "Чебоксары", "Киров", "Нижний Новгород", "Оренбург", "Пенза", "Пермь", "Самара", "Курган", "Екатеринбург", "Тюмень", "Челябинск", "Ханты-Мансийск", "Салехард", "Красноярск", "Иркутск", "Кемерово", "Новосибирск", "Омск", "Томск", "Чита", "Якутск", "Владивосток", "Хабаровск", "Благовещенск", "Петропавловск-Камчатский", "Магадан", "Южно-Сахалинск", "Атланта", "Вашингтон", "Детройт", "Сан-Франциско", "Сиэтл", "Аргентина", "Бразилия", "Канада", "Германия", "Гейдельберг", "Кельн", "Мюнхен", "Франкфурт-на-Майне", "Штутгарт", "Великобритания", "Австрия", "Бельгия", "Болгария", "Венгрия", "Литва", "Нидерланды", "Норвегия", "Польша", "Словакия", "Словения", "Финляндия", "Франция", "Чехия", "Швейцария", "Швеция", "Беер-Шева", "Иерусалим", "Тель-Авив", "Хайфа", "Китай", "Корея", "Япония", "Новая Зеландия", "Киев", "Львов", "Одесса", "Симферополь", "Брест", "Витебск", "Гомель", "Минск", "Могилев", "Алматы", "Астана", "Караганда", "Азербайджан", "Армения", "Грузия", "Туркмения", "Узбекистан", "Уфа", "Берлин", "Гамбург", "Эстония", "Сербия", "Израиль", "Брянск", "Владимир", "Воронеж", "Саратов", "Ульяновск", "Барнаул", "Улан-Удэ", "Лос-Анджелес", "Нью-Йорк", "Дания", "Испания", "Италия", "Латвия", "Киргизия", "Молдова", "Таджикистан", "Объединенные Арабские Эмираты", "Австралия", "Москва", "Бостон", "Магнитогорск", "Набережные Челны", "Новокузнецк", "Новочеркасск", "Сочи", "Тольятти", "Греция", "Севастополь", "Новороссийск", "Таганрог", "Сургут", "Крым", "Турция", "Индия", "Таиланд", "Египет", "Туапсе", "Элиста", "Абакан", "Черкесск", "Грозный", "Анапа", "Мальта", "Хорватия", "Ессентуки", "Кисловодск", "Минеральные Воды", "Арзамас", "Биробиджан", "Амурск", "Евпатория", "Керчь", "Феодосия", "Ялта", "Алушта", "Судак", "Белорецк", "Мексика", "Кипр", "Абхазия", "Южная Осетия"
    )
    private fun handleTrainResponse(trainResponse: TrainSearchResponse) {
        val intent = Intent(this, TrainTicketsActivity::class.java)
        intent.putExtra("trainResponse", trainResponse) // Передайте объект TrainSearchResponse
        startActivity(intent)
    }
    private fun handleFlightResponse(flightResponse: FlightSearchResponse) {
        val intent = Intent(this, FlightTicketsActivity::class.java)
        intent.putExtra("flightResponse", flightResponse)
        startActivity(intent)
    }
}
    private val mainHandler = Handler(Looper.getMainLooper())

private fun showErrorDialogHotels(context: Context, message: String) {
    mainHandler.post {
        AlertDialog.Builder(context)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
}
