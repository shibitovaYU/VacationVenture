package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class Hotel(
    val id: String,
    val title: String,
    val primaryInfo: String?,
    val secondaryInfo: String?,
    val bubbleRating: BubbleRating,
    val count: Int,
    val priceForDisplay: String?,
    val cardPhotos: List<String>,
    val tripAdvisorUrl: String?
) : Parcelable {

    fun getFirstPhotoUrl(width: Int = 1200, height: Int = 800): String? {
        return cardPhotos.firstOrNull()?.toSizedPhotoUrl(width, height)
    }

    fun getAllPhotoUrls(width: Int = 1200, height: Int = 800): List<String> {
        return cardPhotos.map { it.toSizedPhotoUrl(width, height) }
    }

    private fun String.toSizedPhotoUrl(width: Int, height: Int): String {
        return this
            .replace("{width}", width.toString())
            .replace("{height}", height.toString())
            .trim()
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(BubbleRating::class.java.classLoader) ?: BubbleRating("", 0.0),
        parcel.readInt(),
        parcel.readString(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(primaryInfo)
        parcel.writeString(secondaryInfo)
        parcel.writeParcelable(bubbleRating, flags)
        parcel.writeInt(count)
        parcel.writeString(priceForDisplay)
        parcel.writeStringList(cardPhotos)
        parcel.writeString(tripAdvisorUrl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Hotel> {
        override fun createFromParcel(parcel: Parcel): Hotel = Hotel(parcel)
        override fun newArray(size: Int): Array<Hotel?> = arrayOfNulls(size)
    }
}