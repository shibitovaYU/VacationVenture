package com.example.vacationventure.models

import com.example.vacationventure.model.Dates

data class EventData(
    val name: String = "",
    val dates: Dates = Dates(),             // Используем объект Dates
    val _embedded: Embedded = Embedded(),   // Используем объект Embedded
    val images: List<Image> = emptyList(),  // Список изображений
    val url: String = ""
)

data class DatesData(
    val start: String = ""  // Здесь структура может быть детализирована в зависимости от API

) {
    // Конструктор по умолчанию для Firebase
    constructor() : this("")
}

data class EmbeddedData(
    val venue: String = ""
) {
    constructor() : this("")
}

data class ImageData(
    val url: String = "",
) {
    constructor() : this("")
}
