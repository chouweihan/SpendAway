package com.weihan.chou.spendaway

import com.google.firebase.database.Exclude


data class User(
    var username: String? = "",
    var uid: String? = ""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "username" to username,
            "uid" to uid
        )
    }
}