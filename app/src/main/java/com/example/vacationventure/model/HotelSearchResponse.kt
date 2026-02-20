package com.example.vacationventure.model

data class HotelSearchResponse(
    val status: Boolean,
    val message: Any,
    val timestamp: Long,
    val data: HotelData
)

data class HotelData(
    val sortDisclaimer: String,
    val data: List<HotelJson> // Список отелей
)
data class HotelJson(
    val title: String,
    val geoId: Int,
    val documentId: String,
    val trackingItems: String,
    val secondaryText: String
)