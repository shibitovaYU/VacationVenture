package com.example.vacationventure.model

import android.os.Parcel
import android.os.Parcelable

data class FlightSearchResponse(
    val segments: List<FlightSegment>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        mutableListOf<FlightSegment>().apply {
            parcel.readList(this, FlightSegment::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(segments)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<FlightSearchResponse> {
        override fun createFromParcel(parcel: Parcel) = FlightSearchResponse(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FlightSearchResponse?>(size)
    }
}

data class FlightSegment(
    val arrival: String,
    val from: Station,
    val thread: ThreadInfo,
    val departure_platform: String,
    val departure: String,
    val stops: String,
    val departure_terminal: String?,
    val to: Station,
    val has_transfers: Boolean,
    val tickets_info: TicketsInfo,
    val duration: Int,
    val arrival_terminal: String,
    val start_date: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(Station::class.java.classLoader)!!,
        parcel.readParcelable(ThreadInfo::class.java.classLoader)!!,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readParcelable(Station::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(TicketsInfo::class.java.classLoader)!!,
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(arrival)
        parcel.writeParcelable(from, flags)
        parcel.writeParcelable(thread, flags)
        parcel.writeString(departure_platform)
        parcel.writeString(departure)
        parcel.writeString(stops)
        parcel.writeString(departure_terminal)
        parcel.writeParcelable(to, flags)
        parcel.writeByte(if (has_transfers) 1 else 0)
        parcel.writeParcelable(tickets_info, flags)
        parcel.writeInt(duration)
        parcel.writeString(arrival_terminal)
        parcel.writeString(start_date)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<FlightSegment> {
        override fun createFromParcel(parcel: Parcel) = FlightSegment(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FlightSegment?>(size)
    }
}