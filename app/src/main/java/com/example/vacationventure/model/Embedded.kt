package com.example.vacationventure.models

import android.os.Parcel
import android.os.Parcelable

data class Embedded(
    val venues: List<Venue> = listOf()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Venue.CREATOR) ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(venues)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Embedded> {
        override fun createFromParcel(parcel: Parcel): Embedded = Embedded(parcel)
        override fun newArray(size: Int): Array<Embedded?> = arrayOfNulls(size)
    }
}
