package com.example.vacationventure.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface AmadeusApiService {

    @GET("v2/shopping/flight-offers")
    fun searchFlightOffers(
        @Query("originLocationCode") origin: String,
        @Query("destinationLocationCode") destination: String,
        @Query("departureDate") departureDate: String,
        @Query("adults") adults: Int,
        @Header("Authorization") token: String
    ): Call<FlightOffersResponse>
}
