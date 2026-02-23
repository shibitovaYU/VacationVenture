package com.example.vacationventure.network.recommendations

import com.example.vacationventure.model.dto.recommendations.RecoEvent
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Response

interface RecoApi {
    @POST("events")
    suspend fun postEvent(
        @Header("Authorization") authorization: String,
        @Body event: RecoEvent
    ): Response<Unit>
}