package com.nishantdev961.whatsappclone.models

import com.google.firebase.firestore.FieldValue

data class User(

    val name: String,
    val imageUrl: String,
    val thumbImageUrl: String,
    val uid: String,
    val deviceToken: String,
    val status : String,
    val onlineStatus: String,
){
    constructor(): this("", "", "", "", "", "", FieldValue.serverTimestamp().toString())

    constructor(
        name: String,
        imageUrl: String,
        thumbImageUrl: String,
        uid:String,

    ): this(name, imageUrl, thumbImageUrl, uid,
        "",
        "Hey there I'm Using whatsapp!!",
        FieldValue.serverTimestamp().toString())

}