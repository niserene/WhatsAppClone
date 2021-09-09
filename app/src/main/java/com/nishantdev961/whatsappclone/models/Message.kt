package com.nishantdev961.whatsappclone.models

import android.content.Context
import com.nishantdev961.whatsappclone.utils.formatAsHeader
import java.util.*

interface ChatEvent{

    val sendAt: Date
}

data class Message(

    val message: String,
    val senderId: String,
    val messageId: String,
    val type: String = "TEXT",
    val status: Int = 1,
    val liked: Boolean = false,
    override val sendAt: Date = Date()
): ChatEvent{

    constructor(): this("", "","","",1,false, Date())
}

data class DateHeader(

    val context: Context,
    override val sendAt: Date

) :ChatEvent{

    val date: String = sendAt.formatAsHeader(context)
}