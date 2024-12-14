package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class BubbleRating(
    val count: String, // Количество отзывов
    val rating: Double // Рейтинг
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "0", // Чтение count
        parcel.readDouble()         // Чтение rating
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(count)    // Запись count
        parcel.writeDouble(rating)   // Запись rating
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BubbleRating> {
        override fun createFromParcel(parcel: Parcel): BubbleRating {
            return BubbleRating(parcel)
        }

        override fun newArray(size: Int): Array<BubbleRating?> {
            return arrayOfNulls(size)
        }
    }
}

