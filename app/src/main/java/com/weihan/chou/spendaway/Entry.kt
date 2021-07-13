package com.weihan.chou.spendaway

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Entry(
    var location: String? = "",
    var day: Int? = 0,
    var month: Int? = 0,
    var year: Int? = 0,
    var price: Double? = 0.0,
    var content: String? = "",
    var image: Boolean? = null,
    var imagePath: String? = "",
    var entryKey: String? = ""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "location" to location,
            "day" to day,
            "month" to month,
            "year" to year,
            "price" to price,
            "content" to content,
            "image" to image,
            "imagePath" to imagePath,
            "entryKey" to entryKey
        )
    }
}