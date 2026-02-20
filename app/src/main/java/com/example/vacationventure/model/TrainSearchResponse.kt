package com.example.vacationventure.model

import android.os.Parcel
import android.os.Parcelable

data class TrainSearchResponse(
    val segments: List<TrainSegment>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        segments = mutableListOf<TrainSegment>().apply {
            parcel.readTypedList(this, TrainSegment.CREATOR)
        }
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(segments)
    }
    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<TrainSearchResponse> {
        override fun createFromParcel(parcel: Parcel): TrainSearchResponse = TrainSearchResponse(parcel)
        override fun newArray(size: Int): Array<TrainSearchResponse?> = arrayOfNulls(size)
    }
}

data class TrainSegment(
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
    val start_date: String,
    val arrival_platform: String // Добавлено новое поле
) : Parcelable {
    constructor(parcel: Parcel) : this(
        arrival = parcel.readString() ?: "",
        from = parcel.readParcelable(Station::class.java.classLoader)!!,
        thread = parcel.readParcelable(ThreadInfo::class.java.classLoader)!!,
        departure_platform = parcel.readString() ?: "",
        departure = parcel.readString() ?: "",
        stops = parcel.readString() ?: "",
        departure_terminal = parcel.readString(),
        to = parcel.readParcelable(Station::class.java.classLoader)!!,
        has_transfers = parcel.readByte() != 0.toByte(),
        tickets_info = parcel.readParcelable(TicketsInfo::class.java.classLoader)!!,
        duration = parcel.readInt(),
        arrival_terminal = parcel.readString() ?: "",
        start_date = parcel.readString() ?: "",
        arrival_platform = parcel.readString() ?: "" // Чтение нового поля
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
        parcel.writeString(arrival_platform) // Запись нового поля
    }
    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<TrainSegment> {
        override fun createFromParcel(parcel: Parcel): TrainSegment = TrainSegment(parcel)
        override fun newArray(size: Int): Array<TrainSegment?> = arrayOfNulls(size)
    }
}
