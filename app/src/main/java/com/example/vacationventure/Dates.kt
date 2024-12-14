package com.example.vacationventure.models

import android.os.Parcel
import android.os.Parcelable

data class Dates(
    val start: Start = Start()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Start::class.java.classLoader) ?: Start()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(start, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Dates> {
        override fun createFromParcel(parcel: Parcel): Dates = Dates(parcel)
        override fun newArray(size: Int): Array<Dates?> = arrayOfNulls(size)
    }
}
