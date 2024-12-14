package com.example.vacationventure.model

// Главный класс, который содержит список данных о рейсах
data class FlightSearchResponse(
    val data: List<FlightData>
)

// Класс для описания одного рейса
data class FlightData(
    val id: String,
    val itineraries: List<Itinerary>,
    val price: Price
)

// Класс для описания маршрута (туда и обратно)
data class Itinerary(
    val segments: List<Segment>, // Список сегментов рейса (например, каждый перелет может состоять из нескольких сегментов)
    val duration: String // Общая длительность маршрута (туда или обратно)
)

// Класс для описания сегмента рейса (каждый перелет)
data class Segment(
    val departure: Departure,
    val arrival: Arrival,
    val carrierCode: String, // Код авиакомпании
    val duration: String, // Длительность сегмента
    val numberOfStops: Int, // Количество пересадок
    val stopovers: List<Stopover>? // Пересадки, если они есть
)

// Класс для информации о вылете
data class Departure(
    val iataCode: String, // Код аэропорта вылета
    val at: String // Дата и время вылета
)

// Класс для информации о прибытии
data class Arrival(
    val iataCode: String, // Код аэропорта прибытия
    val at: String // Дата и время прибытия
)

// Класс для пересадок (если есть)
data class Stopover(
    val iataCode: String, // Код аэропорта пересадки
    val duration: String // Длительность пересадки
)

// Класс для описания цены
data class Price(
    val total: String // Общая стоимость рейса
)
