package com.example.vacationventure.api

data class FlightOffersResponse(
    val data: List<FlightOffer>
)

data class FlightOffer(
    val id: String,
    val price: Price,
    val itineraries: List<Itinerary>
)

data class Price(
    val total: String,
    val currency: String
)

data class Itinerary(
    val segments: List<Segment>
)

data class Segment(
    val departure: Location,
    val arrival: Location
)

data class Location(
    val iataCode: String,
    val at: String
)
