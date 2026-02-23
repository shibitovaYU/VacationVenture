package com.example.vacationventure.network.tripadvisor

import android.os.Parcel
import android.os.Parcelable
import com.example.vacationventure.model.HotelSearchResponse
import com.example.vacationventure.model.RestaurantSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

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

