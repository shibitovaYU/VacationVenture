package com.example.vacationventure
import android.os.Parcel
import android.os.Parcelable

data class RestaurantDetails(
    val name: String,
    val averageRating: String?,
    val address: String?,
    val menuUrl: String?,
    val openingHours: List<String>?,
    val photos: List<Photo>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createTypedArrayList(Photo.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(averageRating)
        parcel.writeString(address)
        parcel.writeString(menuUrl)
        parcel.writeStringList(openingHours)
        parcel.writeTypedList(photos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RestaurantDetails> {
        override fun createFromParcel(parcel: Parcel): RestaurantDetails {
            return RestaurantDetails(parcel)
        }

        override fun newArray(size: Int): Array<RestaurantDetails?> {
            return arrayOfNulls(size)
        }
    }
}

data class Photo(val url: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }
    }
}
