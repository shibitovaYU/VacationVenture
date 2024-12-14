package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class Hotel(
    val id: String,                           // ID отеля
    val title: String,                        // Название отеля
    val primaryInfo: String?,                 // Основная информация (например, "Free breakfast available")
    val secondaryInfo: String?,               // Вторичная информация (например, район)
    val bubbleRating: BubbleRating,           // Средний рейтинг
    val count: Int,                           // Количество отзывов
    val priceForDisplay: String?,             // Цена для отображения (например, "$219")
    val cardPhotos: List<String>,             // Список URL изображений отеля
    val tripAdvisorUrl: String?               // Ссылка на TripAdvisor
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",                    // Чтение ID отеля
        parcel.readString() ?: "",                    // Чтение названия отеля
        parcel.readString(),                          // Чтение основной информации
        parcel.readString(),                          // Чтение вторичной информации
        parcel.readParcelable(BubbleRating::class.java.classLoader) ?: BubbleRating("", 0.0), // Чтение рейтинга
        parcel.readInt(),                             // Чтение количества отзывов
        parcel.readString(),                          // Чтение цены для отображения
        parcel.createStringArrayList() ?: listOf(),   // Чтение списка изображений
        parcel.readString()                           // Чтение ссылки на TripAdvisor
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)                         // Запись ID отеля
        parcel.writeString(title)                      // Запись названия отеля
        parcel.writeString(primaryInfo)                // Запись основной информации
        parcel.writeString(secondaryInfo)              // Запись вторичной информации
        parcel.writeParcelable(bubbleRating, flags)    // Запись рейтинга
        parcel.writeInt(count)                         // Запись количества отзывов
        parcel.writeString(priceForDisplay)            // Запись цены для отображения
        parcel.writeStringList(cardPhotos)             // Запись списка изображений
        parcel.writeString(tripAdvisorUrl)             // Запись ссылки на TripAdvisor
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Hotel> {
        override fun createFromParcel(parcel: Parcel): Hotel {
            return Hotel(parcel)
        }

        override fun newArray(size: Int): Array<Hotel?> {
            return arrayOfNulls(size)
        }
    }
}