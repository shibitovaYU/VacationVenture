package com.example.vacationventure.model

data class FavoriteItem(
    val id: String,
    val type: FavoriteType,
    val title: String,
    val subtitle: String,
    val details: String,
    val imageUrl: String?,
    val externalUrl: String?
)

enum class FavoriteType {
    ALL,
    RESTAURANT,
    EVENT,
    HOTEL
}
