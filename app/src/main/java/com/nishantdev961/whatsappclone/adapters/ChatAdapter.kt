package com.nishantdev961.whatsappclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nishantdev961.whatsappclone.R
import com.nishantdev961.whatsappclone.models.ChatEvent
import com.nishantdev961.whatsappclone.models.DateHeader
import com.nishantdev961.whatsappclone.models.Message
import com.nishantdev961.whatsappclone.utils.formatAsTime
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.*
import kotlinx.android.synthetic.main.list_item_date_header.view.*


class ChatAdapter(
        private val list: MutableList<ChatEvent>,
        private val mCurrentUid: String
        ): RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when(viewType){
            TEXT_MESSAGE_REC ->{
                MessageViewHolder(inflate(R.layout.list_item_chat_rec_message))
            }
            TEXT_MESSAGE_SENT->{
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
            }
            DATE_HEADER ->{
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else ->{
                MessageViewHolder(inflate(R.layout.list_item_chat_rec_message))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = list[position]){

            is DateHeader ->{
                holder.itemView.dateHeaderText.text = item.date
            }
            is Message ->{
                holder.itemView.apply{
                    contentText.text = item.message
                    contentTimeText.text = item.sendAt.formatAsTime()
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return when(val event = list[position]){
            is Message ->{

                if(event.senderId == mCurrentUid){
                    TEXT_MESSAGE_SENT
                }
                else TEXT_MESSAGE_REC
            }
            is DateHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }

    class DateViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    class MessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    companion object{
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_REC = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }

}