package com.example.vacationventure.model.dto.FlightFavorite

data class FlightFavoriteData(
    val itemId: String = "",       // threadUid|startDate
    val threadUid: String = "",

    val title: String = "",

    val fromCode: String = "",
    val toCode: String = "",
    val fromTitle: String = "",
    val toTitle: String = "",

    val departure: String = "",     // ISO, как из API
    val arrival: String = "",       // ISO, как из API
    val startDate: String = "",     // YYYY-MM-DD
    val duration: Int = 0,          // seconds
    val number: String = "",

    val detailUrl: String = ""
)