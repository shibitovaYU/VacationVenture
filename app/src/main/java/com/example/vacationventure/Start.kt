package com.example.vacationventure.models

import android.os.Parcel
import android.os.Parcelable

data class Start(
    val localDate: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(localDate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Start> {
        override fun createFromParcel(parcel: Parcel): Start = Start(parcel)
        override fun newArray(size: Int): Array<Start?> = arrayOfNulls(size)
    }
}
