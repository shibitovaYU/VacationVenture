package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class Restaurant(
    val locationId: Long,
    val name: String,
    val heroImgUrl: String? = null,

    val averageRating: Double? = null,
    val userReviewCount: Int? = null,

    val currentOpenStatusCategory: String? = null,
    val currentOpenStatusText: String? = null,

    val priceTag: String? = null,
    val cuisines: List<String> = emptyList(),
    val parentGeoName: String? = null,

    val menuUrl: String? = null,
    val reviewSnippet: String? = null,
    val reviewUrl: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        locationId = parcel.readLong(),
        name = parcel.readString().orEmpty(),
        heroImgUrl = parcel.readString(),

        averageRating = parcel.readValue(Double::class.java.classLoader) as? Double,
        userReviewCount = parcel.readValue(Int::class.java.classLoader) as? Int,

        currentOpenStatusCategory = parcel.readString(),
        currentOpenStatusText = parcel.readString(),

        priceTag = parcel.readString(),
        cuisines = parcel.createStringArrayList() ?: emptyList(),
        parentGeoName = parcel.readString(),

        menuUrl = parcel.readString(),
        reviewSnippet = parcel.readString(),
        reviewUrl = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(locationId)
        parcel.writeString(name)
        parcel.writeString(heroImgUrl)

        parcel.writeValue(averageRating)
        parcel.writeValue(userReviewCount)

        parcel.writeString(currentOpenStatusCategory)
        parcel.writeString(currentOpenStatusText)

        parcel.writeString(priceTag)
        parcel.writeStringList(cuisines)
        parcel.writeString(parentGeoName)

        parcel.writeString(menuUrl)
        parcel.writeString(reviewSnippet)
        parcel.writeString(reviewUrl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        override fun createFromParcel(parcel: Parcel): Restaurant = Restaurant(parcel)
        override fun newArray(size: Int): Array<Restaurant?> = arrayOfNulls(size)
    }
}