package com.example.vacationventure.model.dto.recommendations

import com.google.gson.annotations.SerializedName

data class RecoProfileResponse(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("event_count")
    val eventCount: Int,

    @SerializedName("preferred_departure_time")
    val preferredDepartureTime: RecoFeature?,

    @SerializedName("recommended_departure_city")
    val recommendedDepartureCity: RecoFeature?,

    @SerializedName("favorite_airline")
    val favoriteAirline: RecoFeature?
)

data class RecoFeature(
    @SerializedName("value")
    val value: String,

    @SerializedName("score")
    val score: Double
)