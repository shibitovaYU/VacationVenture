package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class Itinerary(
    val departure: String,
    val arrival: String,
    val date: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(departure)
        parcel.writeString(arrival)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Itinerary> {
        override fun createFromParcel(parcel: Parcel): Itinerary {
            return Itinerary(parcel)
        }

        override fun newArray(size: Int): Array<Itinerary?> {
            return arrayOfNulls(size)
        }
    }
}
