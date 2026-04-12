package com.example.vacationventure.model.dto.recommendations

import com.example.vacationventure.model.FlightSegment
import com.google.gson.annotations.SerializedName

data class FlightRankingRequest(
    @SerializedName("flights")
    val flights: List<FlightSegment>
)

data class FlightRankingResponse(
    @SerializedName("ranked_flights")
    val rankedFlights: List<RankedFlight>
)

data class RankedFlight(
    @SerializedName("rank")
    val rank: Int,

    @SerializedName("score")
    val score: Double,

    @SerializedName("flight")
    val flight: FlightSegment
)