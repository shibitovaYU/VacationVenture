package com.example.vacationventure.model

import com.example.vacationventure.Restaurant

data class RestaurantSearchResponse(
    val status: Boolean,
    val message: String,
    val data: RestaurantData
)
data class RestaurantData(
    val totalRecords: Int,
    val totalPages: Int,
    val data: List<Restaurant>
)