package com.example.vacationventure.models

import android.os.Parcel
import android.os.Parcelable

data class Image(
    val url: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image = Image(parcel)
        override fun newArray(size: Int): Array<Image?> = arrayOfNulls(size)
    }
}
