package com.example.vacationventure.model

import android.os.Parcel
import android.os.Parcelable

data class Station(
    val code: String,
    val title: String,
    val popular_title: String,
    val short_title: String,
    val transport_type: String,
    val type: String,
    val station_type: String,
    val station_type_name: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeString(title)
        parcel.writeString(popular_title)
        parcel.writeString(short_title)
        parcel.writeString(transport_type)
        parcel.writeString(type)
        parcel.writeString(station_type)
        parcel.writeString(station_type_name)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel) = Station(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Station?>(size)
    }
}

data class ThreadInfo(
    val uid: String,
    val title: String,
    val number: String,
    val short_title: String,
    val thread_method_link: String,
    val carrier: CarrierInfo,
    val transport_type: String,
    val vehicle: String,
    val transport_subtype: TransportSubtype,
    val express_type: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(CarrierInfo::class.java.classLoader)!!,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(TransportSubtype::class.java.classLoader)!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(title)
        parcel.writeString(number)
        parcel.writeString(short_title)
        parcel.writeString(thread_method_link)
        parcel.writeParcelable(carrier, flags)
        parcel.writeString(transport_type)
        parcel.writeString(vehicle)
        parcel.writeParcelable(transport_subtype, flags)
        parcel.writeString(express_type)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ThreadInfo> {
        override fun createFromParcel(parcel: Parcel) = ThreadInfo(parcel)
        override fun newArray(size: Int) = arrayOfNulls<ThreadInfo?>(size)
    }
}

data class CarrierInfo(
    val code: Int,
    val contacts: String,
    val url: String,
    val logo_svg: String?,
    val title: String,
    val phone: String,
    val codes: CarrierCodes,
    val address: String,
    val logo: String,
    val email: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(CarrierCodes::class.java.classLoader)!!,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(code)
        parcel.writeString(contacts)
        parcel.writeString(url)
        parcel.writeString(logo_svg)
        parcel.writeString(title)
        parcel.writeString(phone)
        parcel.writeParcelable(codes, flags)
        parcel.writeString(address)
        parcel.writeString(logo)
        parcel.writeString(email)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CarrierInfo> {
        override fun createFromParcel(parcel: Parcel) = CarrierInfo(parcel)
        override fun newArray(size: Int) = arrayOfNulls<CarrierInfo?>(size)
    }
}

data class CarrierCodes(
    val icao: String?,
    val sirena: String,
    val iata: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(icao)
        parcel.writeString(sirena)
        parcel.writeString(iata)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CarrierCodes> {
        override fun createFromParcel(parcel: Parcel) = CarrierCodes(parcel)
        override fun newArray(size: Int) = arrayOfNulls<CarrierCodes?>(size)
    }
}

data class TicketsInfo(
    val et_marker: Boolean,
    val places: List<Place>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        mutableListOf<Place>().apply { parcel.readList(this, Place::class.java.classLoader) }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (et_marker) 1 else 0)
        parcel.writeList(places)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TicketsInfo> {
        override fun createFromParcel(parcel: Parcel) = TicketsInfo(parcel)
        override fun newArray(size: Int) = arrayOfNulls<TicketsInfo?>(size)
    }
}

data class Place(
    val currency: String,
    val price: TicketPrice,
    val name: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(TicketPrice::class.java.classLoader)!!,
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(currency)
        parcel.writeParcelable(price, flags)
        parcel.writeString(name)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Place> {
        override fun createFromParcel(parcel: Parcel) = Place(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Place?>(size)
    }
}

data class TicketPrice(
    val cents: Int,
    val whole: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(cents)
        parcel.writeInt(whole)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TicketPrice> {
        override fun createFromParcel(parcel: Parcel) = TicketPrice(parcel)
        override fun newArray(size: Int) = arrayOfNulls<TicketPrice?>(size)
    }
}

data class TransportSubtype(
    val color: String,
    val code: String,
    val title: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(color)
        parcel.writeString(code)
        parcel.writeString(title)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TransportSubtype> {
        override fun createFromParcel(parcel: Parcel) = TransportSubtype(parcel)
        override fun newArray(size: Int) = arrayOfNulls<TransportSubtype?>(size)
    }
}