package com.example.vacationventure

import android.os.Parcel
import android.os.Parcelable

data class FlightTicket(
    val numberOfStops: Int,
    val airlineName: String,
    val airlineLogoUrl: String,
    val departureTime: String,
    val duration: String,
    val price: String,
    val baggageIncluded: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        numberOfStops = parcel.readInt(),
        airlineName = parcel.readString() ?: "",
        airlineLogoUrl = parcel.readString() ?: "",
        departureTime = parcel.readString() ?: "",
        duration = parcel.readString() ?: "",
        price = parcel.readString() ?: "",
        baggageIncluded = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(numberOfStops)
        parcel.writeString(airlineName)
        parcel.writeString(airlineLogoUrl)
        parcel.writeString(departureTime)
        parcel.writeString(duration)
        parcel.writeString(price)
        parcel.writeByte(if (baggageIncluded) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FlightTicket> {
        override fun createFromParcel(parcel: Parcel): FlightTicket {
            return FlightTicket(parcel)
        }

        override fun newArray(size: Int): Array<FlightTicket?> {
            return arrayOfNulls(size)
        }
    }
}
