package com.example.vacationventure.model.dto.recommendations

import com.google.gson.annotations.SerializedName

data class RecoEvent(
    @SerializedName("event_type")
    val eventType: EventType,

    @SerializedName("occurred_at_ms")
    val occurredAtMs: Long,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("search")
    val search: SearchContext,

    @SerializedName("item")
    val item: ItemSnapshot
)

enum class EventType {
    @SerializedName("click") CLICK,
    @SerializedName("favorite") FAVORITE,
    @SerializedName("unfavorite") UNFAVORITE
}

/**
 * Ровно то, что вводит пользователь: откуда / куда / дата.
 */
data class SearchContext(
    @SerializedName("from_code")
    val fromCode: String,

    @SerializedName("to_code")
    val toCode: String,

    @SerializedName("when_date")
    val whenDate: String // "YYYY-MM-DD"
)

/**
 * Снимок данных для отрисовки карточки + link на детали.
 * itemId делай стабильным: thread.uid + "|" + start_date
 */
data class ItemSnapshot(
    @SerializedName("item_id")
    val itemId: String, // "SU-1276_260221_c26_12|2026-02-21"

    @SerializedName("thread_uid")
    val threadUid: String, // segment.thread.uid

    @SerializedName("title")
    val title: String, // segment.thread.title

    @SerializedName("departure_time")
    val departureTime: String, // "HH:mm"

    @SerializedName("departure_station")
    val departureStation: String, // segment.from.title (или "Аэропорт: ...", как в UI)

    @SerializedName("departure_date")
    val departureDate: String, // "YYYY-MM-DD"

    @SerializedName("arrival_time")
    val arrivalTime: String, // "HH:mm"

    @SerializedName("arrival_station")
    val arrivalStation: String, // segment.to.title (или "Аэропорт: ...")

    @SerializedName("arrival_date")
    val arrivalDate: String, // "YYYY-MM-DD"

    @SerializedName("duration_text")
    val durationText: String, // "Длительность: X ч Y мин"

    @SerializedName("detail_url")
    val detailUrl: String // ссылка на travel.yandex.ru
)