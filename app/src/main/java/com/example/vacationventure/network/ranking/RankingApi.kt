package com.example.vacationventure.network.ranking

import com.example.vacationventure.model.dto.recommendations.FlightRankingRequest
import com.example.vacationventure.model.dto.recommendations.FlightRankingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RankingApi {

    @POST("users/me/flights/rank")
    suspend fun rankFlights(
        @Header("Authorization") authorization: String,
        @Body request: FlightRankingRequest
    ): Response<FlightRankingResponse>
}