package com.example.vacationventure.model.dto.recommendations

import com.example.vacationventure.model.FlightSegment

data class FlightRankingRequest(
    val flights: List<FlightSegment>
)

data class FlightRankingResponse(
    val rankedFlights: List<RankedFlight>
)

data class RankedFlight(
    val rank:   Int,
    val score:  Double,
    val flight: FlightSegment
)