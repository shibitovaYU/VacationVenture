package com.example.vacationventure.network.ticketmaster

import com.example.vacationventure.models.Event
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class EventResponse(
    val _embedded: EmbeddedEvents? // Убедитесь, что используется `EmbeddedEvents?` для обработки возможных null-значений
)

data class EmbeddedEvents(
    val events: List<Event> // Список событий
)


interface TicketmasterApi {
    @GET("/discovery/v2/events.json")
    fun searchEvents(
        @Query("apikey") apiKey: String,
        @Query("city") city: String,
        @Query("date") date: String
    ): Call<EventResponse>
}