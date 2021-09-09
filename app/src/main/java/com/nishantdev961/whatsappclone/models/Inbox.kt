package com.nishantdev961.whatsappclone.models

import java.util.*

data class Inbox(

    val message: String,
    var from: String,
    var name: String,
    var image: String,
    val time: Date = Date(),
    var count: Int = 0
){

    constructor(): this("", "", "", "", Date(), 0)
}