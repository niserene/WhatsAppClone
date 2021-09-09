package com.nishantdev961.whatsappclone

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.nishantdev961.whatsappclone.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item.view.*

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    fun bind(user: User, onClick:(name: String, photo:String, id: String)->Unit){
        with(itemView) {

            countText.isVisible = false
            timeText.isVisible = false

            titleText.text = user.name
            subtitleText.text = user.status
            Picasso.get().load(user.thumbImageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(userImageview)

            setOnClickListener{
                onClick.invoke(user.name, user.thumbImageUrl, user.uid)
            }
        }

    }
}