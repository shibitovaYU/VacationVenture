package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class Restaurant(
    val name: String,                       // Имя ресторана
    val restaurantsId: String,              // ID ресторана
    val averageRating: Double,              // Средний рейтинг
    val userReviewCount: Int,               // Количество отзывов пользователей
    val currentOpenStatusText: String?,      // Текстовое описание статуса ресторана
    val priceTag: String?,                  // Уровень цен (может быть null)
    val menuUrl: String?,                   // URL меню (если доступно)
    val heroImgUrl: String?                 // URL главного изображения ресторана
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",                  // Чтение имени ресторана
        parcel.readString() ?: "",                  // Чтение ID ресторана
        parcel.readDouble(),                        // Чтение среднего рейтинга
        parcel.readInt(),                           // Чтение количества отзывов
        parcel.readString() ?: "",                  // Чтение текста статуса
        parcel.readString(),                        // Чтение уровня цен (если есть)
        parcel.readString(),                        // Чтение URL меню (если есть)
        parcel.readString()                         // Чтение URL изображения (если есть)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)                    // Запись имени ресторана
        parcel.writeString(restaurantsId)           // Запись ID ресторана
        parcel.writeDouble(averageRating)           // Запись среднего рейтинга
        parcel.writeInt(userReviewCount)            // Запись количества отзывов
        parcel.writeString(currentOpenStatusText)   // Запись текста статуса
        parcel.writeString(priceTag)                // Запись уровня цен
        parcel.writeString(menuUrl)                 // Запись URL меню
        parcel.writeString(heroImgUrl)              // Запись URL изображения
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        override fun createFromParcel(parcel: Parcel): Restaurant {
            return Restaurant(parcel)
        }

        override fun newArray(size: Int): Array<Restaurant?> {
            return arrayOfNulls(size)
        }
    }
}
