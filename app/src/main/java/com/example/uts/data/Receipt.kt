package com.example.uts.data

import android.os.Parcel
import android.os.Parcelable

data class Receipt(
    val id: String ? = "",
    val idUser: String ? = "",
    val user: String ? = "",
    val title: String ? = "",
    val date: Long ? = 0,
    val description: String ? = "",
    val image: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(idUser)
        parcel.writeString(user)
        parcel.writeString(title)
        parcel.writeLong(date!!)
        parcel.writeString(description)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Receipt> {
        override fun createFromParcel(parcel: Parcel): Receipt {
            return Receipt(parcel)
        }

        override fun newArray(size: Int): Array<Receipt?> {
            return arrayOfNulls(size)
        }
    }

}