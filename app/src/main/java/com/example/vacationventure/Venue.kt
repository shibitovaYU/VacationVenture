package com.example.vacationventure.models

import android.os.Parcel
import android.os.Parcelable

data class Venue(
    val name: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Venue> {
        override fun createFromParcel(parcel: Parcel): Venue = Venue(parcel)
        override fun newArray(size: Int): Array<Venue?> = arrayOfNulls(size)
    }
}
