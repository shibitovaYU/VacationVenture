package com.example.vacationventure.network

import com.example.vacationventure.model.FlightSearchResponse
import com.example.vacationventure.model.TrainSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


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