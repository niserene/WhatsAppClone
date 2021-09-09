package com.nishantdev961.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nishantdev961.whatsappclone.adapters.ChatAdapter
import com.nishantdev961.whatsappclone.models.*
import com.nishantdev961.whatsappclone.utils.isSameDayAs
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiProvider
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val UID = "uid"
const val NAME = "name"
const val PHOTO = "photo"
class ChatActivity : AppCompatActivity() {

    private val friendId by lazy {
        intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val image by lazy{
        intent.getStringExtra(PHOTO)
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }
    lateinit var currentUser: User

    private val messages = mutableListOf<ChatEvent>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        initSetup()
        listenToMessages()
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid)
            .get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }
    }

    private fun initSetup() {

        Picasso.get().load(image).into(userImageView)
        usernameText.text = name

        chatAdapter = ChatAdapter(messages, mCurrentUid)

        chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        sendBtn.setOnClickListener{
            messageText.text?.let { 
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
    }

    private fun listenToMessages(){

        getMessages(friendId.toString())
                .orderByKey()
                .addChildEventListener(object: ChildEventListener{

                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val msg = snapshot.getValue(Message::class.java)!!
                        addMessage(msg)
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
    }

    private fun addMessage(msg: Message) {

        val eventBefore = messages.lastOrNull()
        if((eventBefore != null && !eventBefore.sendAt.isSameDayAs(msg.sendAt)) || eventBefore == null){
            messages.add(DateHeader(this, msg.sendAt))
        }
        messages.add(msg)
        chatAdapter.notifyItemInserted(messages.size-1)
        chatsRecyclerView.scrollToPosition(messages.size -1)
    }

    private fun sendMessage(message: String) {

        val id = getMessages(friendId.toString()).push().key
        val messageMap = Message(message, mCurrentUid.toString(), id.toString())
        getMessages(friendId.toString()).child(id.toString()).setValue(messageMap)
                .addOnSuccessListener {
                    Log.d("CHATS", "done")
                }
                .addOnFailureListener{
                    Log.d("CHATS", it.localizedMessage)
                }
        updateLastMessage(messageMap)
    }

    private fun updateLastMessage(message: Message) {

        val inboxMap = Inbox(
                message.message,
                friendId.toString(),
                name!!,
                image!!
        )
        getInbox(mCurrentUid, friendId.toString()).setValue(inboxMap)
                .addOnSuccessListener {
                    getInbox(friendId.toString(), mCurrentUid).addListenerForSingleValueEvent(
                            object: ValueEventListener{

                                override fun onDataChange(snapshot: DataSnapshot) {

                                    val value = snapshot.getValue(Inbox::class.java)
                                    inboxMap.apply {
                                        from = message.senderId
                                        name = currentUser.name
                                        image = currentUser.thumbImageUrl
                                        count = 1
                                    }
                                    value?.let {
                                        if(it.from == message.senderId){
                                            inboxMap.count = value.count + 1
                                        }
                                    }

                                    getInbox(friendId.toString(), mCurrentUid).setValue(inboxMap)
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }

                            }
                    )
                }
                .addOnFailureListener{
                    Log.d("CHATS", it.localizedMessage)
                }
    }

    private fun markAsRead(){
        getInbox(friendId.toString(), mCurrentUid).child("count").setValue(0)
    }

    private fun getInbox(toUser: String, fromUser: String):DatabaseReference{
        return db.reference.child("chats/$toUser/$fromUser")
    }

    private fun getMessages(friendId: String):DatabaseReference{
        return db.reference.child("messages/${getId(friendId)}")
    }

    private fun getId(friendId: String): String{
        return if(friendId > mCurrentUid)
            mCurrentUid+friendId
        else friendId+mCurrentUid
    }
}