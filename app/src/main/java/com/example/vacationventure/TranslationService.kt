package com.example.vacationventure

import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationService {
    @GET("translate")
    suspend fun translate(
        @Query("key") apiKey: String,
        @Query("text") text: String,
        @Query("lang") targetLang: String
    ): TranslationResponse
}

data class TranslationResponse(val text: List<String>)
