package com.weihan.chou.spendaway

import android.os.Parcel
import android.os.Parcelable

data class EntryPH(val loc:String, val d:Int, val m:Int, val y:Int, val p:Double, val con:String, val im:Boolean, val imP:String, val eK: String) :
    Parcelable {
    var location: String = loc
    var day: Int = d
    var month: Int = m
    var year: Int = y
    var price: Double = p
    var content: String = con
    var image: Boolean = im
    var imagePath: String = imP
    var entryKey: String = eK

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString()
    ) {
        location = parcel.readString()
        day = parcel.readInt()
        month = parcel.readInt()
        year = parcel.readInt()
        price = parcel.readDouble()
        content = parcel.readString()
        image = parcel.readByte() != 0.toByte()
        imagePath = parcel.readString()
        entryKey = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(loc)
        parcel.writeInt(d)
        parcel.writeInt(m)
        parcel.writeInt(y)
        parcel.writeDouble(p)
        parcel.writeString(con)
        parcel.writeByte(if (im) 1 else 0)
        parcel.writeString(imP)
        parcel.writeString(eK)
        parcel.writeString(location)
        parcel.writeInt(day)
        parcel.writeInt(month)
        parcel.writeInt(year)
        parcel.writeDouble(price)
        parcel.writeString(content)
        parcel.writeByte(if (image) 1 else 0)
        parcel.writeString(imagePath)
        parcel.writeString(entryKey)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EntryPH> {
        override fun createFromParcel(parcel: Parcel): EntryPH {
            return EntryPH(parcel)
        }

        override fun newArray(size: Int): Array<EntryPH?> {
            return arrayOfNulls(size)
        }
    }
}