package com.example.vacationventure.models

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class Event(
    val name: String,
    val dates: Dates,
    val _embedded: Embedded,
    val images: List<Image>,
    val url: String // Добавляем поле для URL
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(Dates::class.java.classLoader) ?: Dates(),
        parcel.readParcelable(Embedded::class.java.classLoader) ?: Embedded(),
        parcel.createTypedArrayList(Image.CREATOR) ?: listOf(),
        parcel.readString() ?: "" // Чтение url из Parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(dates, flags)
        parcel.writeParcelable(_embedded, flags)
        parcel.writeTypedList(images)
        parcel.writeString(url) // Запись url в Parcel
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event = Event(parcel)
        override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
    }
}
