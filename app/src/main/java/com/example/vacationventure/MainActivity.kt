package com.example.vacationventure

import android.app.DatePickerDialog
import android.os.Bundle
import android.net.Uri
import org.json.JSONArray
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.ImageView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import android.widget.EditText
import android.widget.ListView
import kotlin.concurrent.timer
import android.widget.ImageButton
import java.nio.charset.StandardCharsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.example.vacationventure.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import com.example.vacationventure.BubbleRating
import android.util.Log
import java.util.Calendar
import android.content.Intent
import android.app.AlertDialog
import android.widget.NumberPicker
import android.view.LayoutInflater
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import android.os.Parcel
import android.os.Parcelable
import retrofit2.http.Header
import kotlinx.parcelize.Parcelize
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import java.net.URLEncoder
import javax.net.ssl.*
import java.security.SecureRandom
import org.json.JSONObject
import okhttp3.Request
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.io.IOException
import kotlin.concurrent.thread
import android.widget.Toast
import com.google.gson.Gson
import okhttp3.Response as OkHttpResponse
import okhttp3.*
import android.os.StrictMode
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.vacationventure.models.Event
import com.example.vacationventure.models.Embedded
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.vacationventure.FlightTicket
import okhttp3.*
import java.util.*
import com.google.gson.GsonBuilder


private lateinit var retrofit: Retrofit

private const val TAG = "FlightSearch"

class MainActivity : AppCompatActivity() {

    // Примеры городов и соответствующих кодов
    private val cities = arrayOf(
        "Москва",
        "Санкт-Петербург",
        "Нью-Йорк",
        "Лондон",
        "Берлин",
        "Париж",
        "Токио",
        "Сидней",
        "Рим",
        "Мадрид",
        "Торонто",
        "Дубай",
        "Брюссель",
        "Будапешт",
        "Копенгаген",
        "Стокгольм",
        "Цюрих",
        "Сеул",
        "Бангкок",
        "Гонконг",
        "Мехико",
        "Лос-Анджелес",
        "Чикаго",
        "Хьюстон",
        "Филадельфия",
        "Сан-Франциско",
        "Сан-Диего",
        "Сиэтл",
        "Вашингтон",
        "Бостон",
        "Даллас",
        "Атланта",
        "Саров",
        "Нижний Новгород",
        "Белорецк",
        "Тюмень",
        "Красноярск",
        "Владивосток",
        "Новосибирск",
        "Сочи",
        "Красноярск",
        "Тверь",
        "Магадан",
        "Владимир",
        "Волгоград",
        "Ростов-на-Дону",
        "Каазнь",
        "Адлер",
        "Вологда",
        "Астрахань",
        "Архангельск",
        "Петрозаводск"
    )
    private val englishCities = arrayOf(
        "Moscow",
        "Saint Petersburg",
        "New York",
        "London",
        "Berlin",
        "Paris",
        "Tokyo",
        "Sydney",
        "Rome",
        "Madrid",
        "Toronto",
        "Dubai",
        "Brussels",
        "Budapest",
        "Copenhagen",
        "Stockholm",
        "Zurich",
        "Seoul",
        "Bangkok",
        "Hong Kong",
        "Mexico City",
        "Los Angeles",
        "Chicago",
        "Houston",
        "Philadelphia",
        "San Francisco",
        "San Diego",
        "Seattle",
        "Washington D.C.",
        "Boston",
        "Dallas",
        "Atlanta"
    )
    private val countryCodes = arrayOf(
        "RU", // Москва
        "RU", // Санкт-Петербург
        "US", // Нью-Йорк
        "GB", // Лондон
        "DE", // Берлин
        "FR", // Париж
        "JP", // Токио
        "AU", // Сидней
        "IT", // Рим
        "ES", // Мадрид
        "CA", // Торонто
        "AE", // Дубай
        "BE", // Брюссель
        "HU", // Будапешт
        "DK", // Копенгаген
        "SE", // Стокгольм
        "CH", // Цюрих
        "KR", // Сеул
        "TH", // Бангкок
        "HK", // Гонконг
        "MX", // Мехико
        "US", // Лос-Анджелес
        "US", // Чикаго
        "US", // Хьюстон
        "US", // Филадельфия
        "US", // Сан-Франциско
        "US", // Сан-Диего
        "US", // Сиэтл
        "US", // Вашингтон
        "US", // Бостон
        "US", // Даллас
        "US"  // Атланта
    )

    data class HotelSearchResponse(
        val status: Boolean,
        val message: Any,
        val timestamp: Long,
        val data: HotelData
    )
    data class Agent(
        val title: String
    )
    data class HotelData(
        val sortDisclaimer: String,
        val data: List<HotelJson> // Список отелей
    )
    data class HotelJson(
        val title: String,
        val geoId: Int,
        val documentId: String,
        val trackingItems: String,
        val secondaryText: String
    )
    data class CardPhoto(
        val sizes: PhotoSizes
    )
    data class PhotoSizes(
        val urlTemplate: String // Или другие нужные поля
    )
    private lateinit var eventListView: ListView // Объявляем переменную для ListView
    private lateinit var eventAdapter: EventAdapter
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

    data class TrainSearchResponse(
        val segments: List<TrainSegment>
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            segments = mutableListOf<TrainSegment>().apply {
                parcel.readTypedList(this, TrainSegment.CREATOR)
            }
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeTypedList(segments)
        }
        override fun describeContents(): Int = 0
        companion object CREATOR : Parcelable.Creator<TrainSearchResponse> {
            override fun createFromParcel(parcel: Parcel): TrainSearchResponse = TrainSearchResponse(parcel)
            override fun newArray(size: Int): Array<TrainSearchResponse?> = arrayOfNulls(size)
        }
    }

    data class FlightSearchResponse(
        val segments: List<FlightSegment>
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            mutableListOf<FlightSegment>().apply {
                parcel.readList(this, FlightSegment::class.java.classLoader)
            }
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeList(segments)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<FlightSearchResponse> {
            override fun createFromParcel(parcel: Parcel) = FlightSearchResponse(parcel)
            override fun newArray(size: Int) = arrayOfNulls<FlightSearchResponse?>(size)
        }
    }

    data class TrainSegment(
        val arrival: String,
        val from: Station,
        val thread: ThreadInfo,
        val departure_platform: String,
        val departure: String,
        val stops: String,
        val departure_terminal: String?,
        val to: Station,
        val has_transfers: Boolean,
        val tickets_info: TicketsInfo,
        val duration: Int,
        val arrival_terminal: String,
        val start_date: String,
        val arrival_platform: String // Добавлено новое поле
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            arrival = parcel.readString() ?: "",
            from = parcel.readParcelable(Station::class.java.classLoader)!!,
            thread = parcel.readParcelable(ThreadInfo::class.java.classLoader)!!,
            departure_platform = parcel.readString() ?: "",
            departure = parcel.readString() ?: "",
            stops = parcel.readString() ?: "",
            departure_terminal = parcel.readString(),
            to = parcel.readParcelable(Station::class.java.classLoader)!!,
            has_transfers = parcel.readByte() != 0.toByte(),
            tickets_info = parcel.readParcelable(TicketsInfo::class.java.classLoader)!!,
            duration = parcel.readInt(),
            arrival_terminal = parcel.readString() ?: "",
            start_date = parcel.readString() ?: "",
            arrival_platform = parcel.readString() ?: "" // Чтение нового поля
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(arrival)
            parcel.writeParcelable(from, flags)
            parcel.writeParcelable(thread, flags)
            parcel.writeString(departure_platform)
            parcel.writeString(departure)
            parcel.writeString(stops)
            parcel.writeString(departure_terminal)
            parcel.writeParcelable(to, flags)
            parcel.writeByte(if (has_transfers) 1 else 0)
            parcel.writeParcelable(tickets_info, flags)
            parcel.writeInt(duration)
            parcel.writeString(arrival_terminal)
            parcel.writeString(start_date)
            parcel.writeString(arrival_platform) // Запись нового поля
        }
        override fun describeContents(): Int = 0
        companion object CREATOR : Parcelable.Creator<TrainSegment> {
            override fun createFromParcel(parcel: Parcel): TrainSegment = TrainSegment(parcel)
            override fun newArray(size: Int): Array<TrainSegment?> = arrayOfNulls(size)
        }
    }


    data class FlightSegment(
        val arrival: String,
        val from: Station,
        val thread: ThreadInfo,
        val departure_platform: String,
        val departure: String,
        val stops: String,
        val departure_terminal: String?,
        val to: Station,
        val has_transfers: Boolean,
        val tickets_info: TicketsInfo,
        val duration: Int,
        val arrival_terminal: String,
        val start_date: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readParcelable(Station::class.java.classLoader)!!,
            parcel.readParcelable(ThreadInfo::class.java.classLoader)!!,
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readParcelable(Station::class.java.classLoader)!!,
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(TicketsInfo::class.java.classLoader)!!,
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(arrival)
            parcel.writeParcelable(from, flags)
            parcel.writeParcelable(thread, flags)
            parcel.writeString(departure_platform)
            parcel.writeString(departure)
            parcel.writeString(stops)
            parcel.writeString(departure_terminal)
            parcel.writeParcelable(to, flags)
            parcel.writeByte(if (has_transfers) 1 else 0)
            parcel.writeParcelable(tickets_info, flags)
            parcel.writeInt(duration)
            parcel.writeString(arrival_terminal)
            parcel.writeString(start_date)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<FlightSegment> {
            override fun createFromParcel(parcel: Parcel) = FlightSegment(parcel)
            override fun newArray(size: Int) = arrayOfNulls<FlightSegment?>(size)
        }
    }

    data class Station(
        val code: String,
        val title: String,
        val popular_title: String,
        val short_title: String,
        val transport_type: String,
        val type: String,
        val station_type: String,
        val station_type_name: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(code)
            parcel.writeString(title)
            parcel.writeString(popular_title)
            parcel.writeString(short_title)
            parcel.writeString(transport_type)
            parcel.writeString(type)
            parcel.writeString(station_type)
            parcel.writeString(station_type_name)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<Station> {
            override fun createFromParcel(parcel: Parcel) = Station(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Station?>(size)
        }
    }

    data class ThreadInfo(
        val uid: String,
        val title: String,
        val number: String,
        val short_title: String,
        val thread_method_link: String,
        val carrier: CarrierInfo,
        val transport_type: String,
        val vehicle: String,
        val transport_subtype: TransportSubtype,
        val express_type: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readParcelable(CarrierInfo::class.java.classLoader)!!,
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readParcelable(TransportSubtype::class.java.classLoader)!!,
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(uid)
            parcel.writeString(title)
            parcel.writeString(number)
            parcel.writeString(short_title)
            parcel.writeString(thread_method_link)
            parcel.writeParcelable(carrier, flags)
            parcel.writeString(transport_type)
            parcel.writeString(vehicle)
            parcel.writeParcelable(transport_subtype, flags)
            parcel.writeString(express_type)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<ThreadInfo> {
            override fun createFromParcel(parcel: Parcel) = ThreadInfo(parcel)
            override fun newArray(size: Int) = arrayOfNulls<ThreadInfo?>(size)
        }
    }

    data class CarrierInfo(
        val code: Int,
        val contacts: String,
        val url: String,
        val logo_svg: String?,
        val title: String,
        val phone: String,
        val codes: CarrierCodes,
        val address: String,
        val logo: String,
        val email: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readParcelable(CarrierCodes::class.java.classLoader)!!,
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(code)
            parcel.writeString(contacts)
            parcel.writeString(url)
            parcel.writeString(logo_svg)
            parcel.writeString(title)
            parcel.writeString(phone)
            parcel.writeParcelable(codes, flags)
            parcel.writeString(address)
            parcel.writeString(logo)
            parcel.writeString(email)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<CarrierInfo> {
            override fun createFromParcel(parcel: Parcel) = CarrierInfo(parcel)
            override fun newArray(size: Int) = arrayOfNulls<CarrierInfo?>(size)
        }
    }

    data class CarrierCodes(
        val icao: String?,
        val sirena: String,
        val iata: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(icao)
            parcel.writeString(sirena)
            parcel.writeString(iata)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<CarrierCodes> {
            override fun createFromParcel(parcel: Parcel) = CarrierCodes(parcel)
            override fun newArray(size: Int) = arrayOfNulls<CarrierCodes?>(size)
        }
    }

    data class TicketsInfo(
        val et_marker: Boolean,
        val places: List<Place>
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            mutableListOf<Place>().apply { parcel.readList(this, Place::class.java.classLoader) }
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByte(if (et_marker) 1 else 0)
            parcel.writeList(places)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<TicketsInfo> {
            override fun createFromParcel(parcel: Parcel) = TicketsInfo(parcel)
            override fun newArray(size: Int) = arrayOfNulls<TicketsInfo?>(size)
        }
    }

    data class Place(
        val currency: String,
        val price: TicketPrice,
        val name: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readParcelable(TicketPrice::class.java.classLoader)!!,
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(currency)
            parcel.writeParcelable(price, flags)
            parcel.writeString(name)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<Place> {
            override fun createFromParcel(parcel: Parcel) = Place(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Place?>(size)
        }
    }

    data class TicketPrice(
        val cents: Int,
        val whole: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(cents)
            parcel.writeInt(whole)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<TicketPrice> {
            override fun createFromParcel(parcel: Parcel) = TicketPrice(parcel)
            override fun newArray(size: Int) = arrayOfNulls<TicketPrice?>(size)
        }
    }

    data class TransportSubtype(
        val color: String,
        val code: String,
        val title: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(color)
            parcel.writeString(code)
            parcel.writeString(title)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<TransportSubtype> {
            override fun createFromParcel(parcel: Parcel) = TransportSubtype(parcel)
            override fun newArray(size: Int) = arrayOfNulls<TransportSubtype?>(size)
        }
    }

    data class Page(
        val size: Int,
        val totalElements: Int,
        val totalPages: Int,
        val number: Int
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
    data class RestaurantDetails(
        val name: String,
        val averageRating: String?,
        val address: String?,
        val menuUrl: String?,
        val openingHours: List<String>?, // Это список строк
        val photos: List<Photo>? // Это список объектов Photo
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
    data class AccessTokenResponse(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("token_type") val tokenType: String,
        @SerializedName("expires_in") val expiresIn: Int
    )
    interface FoursquareApiService {
        @GET("v3/places/search")
        fun searchRestaurants(
            @Header("Authorization") authToken: String,
            @Query("near") city: String,  // Поиск по городу
            @Query("categories") categories: String,  // Поиск по типу кухни
            @Query("limit") limit: Int = 1000           // Лимит результатов (например, 20 ресторанов)
        ): Call<RestaurantSearchResponse>
    }
    interface YandexRaspService {
        @GET("v3.0/search/")
        fun searchTrain(
            @Query("from") from: String,
            @Query("to") to: String,
            @Query("apikey") apiKey: String,
            @Query("date") date: String? = null,
            @Query("transport_types") transportTypes: String = "train",
            @Query("format") format: String? = "json"
        ): Call<TrainSearchResponse>

        @GET("v3.0/search/")
        fun searchFlight(
            @Query("from") from: String,
            @Query("to") to: String,
            @Query("apikey") apiKey: String,
            @Query("date") date: String? = null,
            @Query("transport_types") transportTypes: String = "plane",
            @Query("format") format: String? = "json"
        ): Call<FlightSearchResponse>
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
    private val cityTranslations = mapOf(
        "Москва" to "Moscow",
        "Амстердам" to "Amsterdam",
        "Афины" to "Athens",
        "Барселона" to "Barcelona",
        "Берлин" to "Berlin",
        "Брюссель" to "Brussels",
        "Будапешт" to "Budapest",
        "Варшава" to "Warsaw",
        "Вена" to "Vienna",
        "Гамбург" to "Hamburg",
        "Женева" to "Geneva",
        "Копенгаген" to "Copenhagen",
        "Лиссабон" to "Lisbon",
        "Лондон" to "London",
        "Мадрид" to "Madrid",
        "Милан" to "Milan",
        "Мюнхен" to "Munich",
        "Неаполь" to "Naples",
        "Осло" to "Oslo",
        "Париж" to "Paris",
        "Прага" to "Prague",
        "Рим" to "Rome",
        "Стокгольм" to "Stockholm",
        "Хельсинки" to "Helsinki",
        "Цюрих" to "Zurich",
        "Эдинбург" to "Edinburgh",
        "Флоренция" to "Florence",
        "Венеция" to "Venice",
        "Манчестер" to "Manchester",
        "Лион" to "Lyon",
        "Ницца" to "Nice",
        "Штутгарт" to "Stuttgart",

        // Северная Америка (США и Канада)
        "Атланта" to "Atlanta",
        "Бостон" to "Boston",
        "Вашингтон" to "Washington",
        "Ванкувер" to "Vancouver",
        "Даллас" to "Dallas",
        "Денвер" to "Denver",
        "Лас-Вегас" to "Las Vegas",
        "Лос-Анджелес" to "Los Angeles",
        "Майами" to "Miami",
        "Монреаль" to "Montreal",
        "Нью-Йорк" to "New York",
        "Орландо" to "Orlando",
        "Сан-Франциско" to "San Francisco",
        "Сан-Диего" to "San Diego",
        "Сиэтл" to "Seattle",
        "Торонто" to "Toronto",
        "Чикаго" to "Chicago",
        "Хьюстон" to "Houston",
        "Филадельфия" to "Philadelphia",
        "Финикс" to "Phoenix",

        // Другие крупные города
        "Буэнос-Айрес" to "Buenos Aires",
        "Сантьяго" to "Santiago",
        "Сидней" to "Sydney",
        "Мельбурн" to "Melbourne",
        "Токио" to "Tokyo",
        "Сеул" to "Seoul",
        "Гонконг" to "Hong Kong",
        "Сингапур" to "Singapore",
        "Дубай" to "Dubai",
        "Йоханнесбург" to "Johannesburg",
        "Кейптаун" to "Cape Town"
    )
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
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
    private val cityMap = mapOf(
        "Москва" to Pair("Moscow", "RU"),
        "Санкт-Петербург" to Pair("Saint Petersburg", "RU"),
        "Нью-Йорк" to Pair("New York", "US"),
        "Лондон" to Pair("London", "GB"),
        "Берлин" to Pair("Berlin", "DE"),
        "Париж" to Pair("Paris", "FR"),
        "Токио" to Pair("Tokyo", "JP"),
        "Сидней" to Pair("Sydney", "AU"),
        "Рим" to Pair("Rome", "IT"),
        "Мадрид" to Pair("Madrid", "ES"),
        "Торонто" to Pair("Toronto", "CA"),
        "Дубай" to Pair("Dubai", "AE"),
        "Брюссель" to Pair("Brussels", "BE"),
        "Будапешт" to Pair("Budapest", "HU"),
        "Копенгаген" to Pair("Copenhagen", "DK"),
        "Стокгольм" to Pair("Stockholm", "SE"),
        "Цюрих" to Pair("Zurich", "CH"),
        "Сеул" to Pair("Seoul", "KR"),
        "Бангкок" to Pair("Bangkok", "TH"),
        "Гонконг" to Pair("Hong Kong", "HK"),
        "Мехико" to Pair("Mexico City", "MX"),
        "Лос-Анджелес" to Pair("Los Angeles", "US"),
        "Чикаго" to Pair("Chicago", "US"),
        "Хьюстон" to Pair("Houston", "US"),
        "Филадельфия" to Pair("Philadelphia", "US"),
        "Сан-Франциско" to Pair("San Francisco", "US"),
        "Сан-Диего" to Pair("San Diego", "US"),
        "Сиэтл" to Pair("Seattle", "US"),
        "Вашингтон" to Pair("Washington D.C.", "US"),
        "Бостон" to Pair("Boston", "US"),
        "Даллас" to Pair("Dallas", "US"),
        "Атланта" to Pair("Atlanta", "US")
    )
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
    val cityCodeMapTickets = mapOf(
        "Санкт-Петербург" to "c2",
        "Белгород" to "c4",
        "Иваново" to "c5",
        "Калуга" to "c6",
        "Кострома" to "c7",
        "Курск" to "c8",
        "Липецк" to "c9",
        "Орел" to "c10",
        "Рязань" to "c11",
        "Смоленск" to "c12",
        "Тамбов" to "c13",
        "Тверь" to "c14",
        "Тула" to "c15",
        "Ярославль" to "c16",
        "Петрозаводск" to "c18",
        "Сыктывкар" to "c19",
        "Архангельск" to "c20",
        "Вологда" to "c21",
        "Калининград" to "c22",
        "Мурманск" to "c23",
        "Великий Новгород" to "c24",
        "Псков" to "c25",
        "Махачкала" to "c28",
        "Нальчик" to "c30",
        "Владикавказ" to "c33",
        "Краснодар" to "c35",
        "Ставрополь" to "c36",
        "Астрахань" to "c37",
        "Волгоград" to "c38",
        "Ростов-на-Дону" to "c39",
        "Йошкар-Ола" to "c41",
        "Саранск" to "c42",
        "Казань" to "c43",
        "Ижевск" to "c44",
        "Чебоксары" to "c45",
        "Киров" to "c46",
        "Нижний Новгород" to "c47",
        "Оренбург" to "c48",
        "Пенза" to "c49",
        "Пермь" to "c50",
        "Самара" to "c51",
        "Курган" to "c53",
        "Екатеринбург" to "c54",
        "Тюмень" to "c55",
        "Челябинск" to "c56",
        "Ханты-Мансийск" to "c57",
        "Салехард" to "c58",
        "Красноярск" to "c62",
        "Иркутск" to "c63",
        "Кемерово" to "c64",
        "Новосибирск" to "c65",
        "Омск" to "c66",
        "Томск" to "c67",
        "Чита" to "c68",
        "Якутск" to "c74",
        "Владивосток" to "c75",
        "Хабаровск" to "c76",
        "Благовещенск" to "c77",
        "Петропавловск-Камчатский" to "c78",
        "Магадан" to "c79",
        "Южно-Сахалинск" to "c80",
        "Атланта" to "c86",
        "Вашингтон" to "c87",
        "Детройт" to "c89",
        "Сан-Франциско" to "c90",
        "Сиэтл" to "c91",
        "Аргентина" to "c93",
        "Бразилия" to "c94",
        "Канада" to "c95",
        "Германия" to "c96",
        "Гейдельберг" to "c97",
        "Кельн" to "c98",
        "Мюнхен" to "c99",
        "Франкфурт-на-Майне" to "c100",
        "Штутгарт" to "c101",
        "Великобритания" to "c102",
        "Австрия" to "c113",
        "Бельгия" to "c114",
        "Болгария" to "c115",
        "Венгрия" to "c116",
        "Литва" to "c117",
        "Нидерланды" to "c118",
        "Норвегия" to "c119",
        "Польша" to "c120",
        "Словакия" to "c121",
        "Словения" to "c122",
        "Финляндия" to "c123",
        "Франция" to "c124",
        "Чехия" to "c125",
        "Швейцария" to "c126",
        "Швеция" to "c127",
        "Беер-Шева" to "c129",
        "Иерусалим" to "c130",
        "Тель-Авив" to "c131",
        "Хайфа" to "c132",
        "Китай" to "c134",
        "Корея" to "c135",
        "Япония" to "c137",
        "Новая Зеландия" to "c139",
        "Киев" to "c143",
        "Львов" to "c144",
        "Одесса" to "c145",
        "Симферополь" to "c146",
        "Брест" to "c153",
        "Витебск" to "c154",
        "Гомель" to "c155",
        "Минск" to "c157",
        "Могилев" to "c158",
        "Алматы" to "c162",
        "Астана" to "c163",
        "Караганда" to "c164",
        "Азербайджан" to "c167",
        "Армения" to "c168",
        "Грузия" to "c169",
        "Туркмения" to "c170",
        "Узбекистан" to "c171",
        "Уфа" to "c172",
        "Берлин" to "c177",
        "Гамбург" to "c178",
        "Эстония" to "c179",
        "Сербия" to "c180",
        "Израиль" to "c181",
        "Брянск" to "c191",
        "Владимир" to "c192",
        "Воронеж" to "c193",
        "Саратов" to "c194",
        "Ульяновск" to "c195",
        "Барнаул" to "c197",
        "Улан-Удэ" to "c198",
        "Лос-Анджелес" to "c200",
        "Нью-Йорк" to "c202",
        "Дания" to "c203",
        "Испания" to "c204",
        "Италия" to "c205",
        "Латвия" to "c206",
        "Киргизия" to "c207",
        "Молдова" to "c208",
        "Таджикистан" to "c209",
        "Объединенные Арабские Эмираты" to "c210",
        "Австралия" to "c211",
        "Москва" to "c213",
        "Бостон" to "c223",
        "Магнитогорск" to "c235",
        "Набережные Челны" to "c236",
        "Новокузнецк" to "c237",
        "Новочеркасск" to "c238",
        "Сочи" to "c239",
        "Тольятти" to "c240",
        "Греция" to "c246",
        "Севастополь" to "c959",
        "Новороссийск" to "c970",
        "Таганрог" to "c971",
        "Сургут" to "c973",
        "Крым" to "c977",
        "Турция" to "c983",
        "Индия" to "c994",
        "Таиланд" to "c995",
        "Египет" to "c1056",
        "Туапсе" to "c1058",
        "Элиста" to "c1094",
        "Абакан" to "c1095",
        "Черкесск" to "c1104",
        "Грозный" to "c1106",
        "Анапа" to "c1107",
        "Мальта" to "c10069",
        "Хорватия" to "c10083",
        "Ессентуки" to "c11057",
        "Кисловодск" to "c11062",
        "Минеральные Воды" to "c11063",
        "Арзамас" to "c11080",
        "Биробиджан" to "c11393",
        "Амурск" to "c11451",
        "Евпатория" to "c11463",
        "Керчь" to "c11464",
        "Феодосия" to "c11469",
        "Ялта" to "c11470",
        "Алушта" to "c11471",
        "Судак" to "c11472",
        "Белорецк" to "c20259",
        "Мексика" to "c20271",
        "Кипр" to "c20574",
        "Абхазия" to "c29386",
        "Южная Осетия" to "c29387"
    )
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
        handler.postDelayed(timeoutRunnable, 60000)

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
