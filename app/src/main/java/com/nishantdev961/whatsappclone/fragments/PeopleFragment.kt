package com.nishantdev961.whatsappclone.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nishantdev961.whatsappclone.*
import com.nishantdev961.whatsappclone.models.User
import kotlinx.android.synthetic.main.fragment_chats.*
import java.lang.Exception

private const val Normal_View_Type = 1
private const val Deleted_View_Type = 2

class PeopleFragment: Fragment() {

    lateinit var mAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val baseQuery by lazy{
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chats, container,false)
    }

    private fun setupAdapter() {

        val config = PagedList.Config.Builder()
            .setPrefetchDistance(2)
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()

        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery, config, User::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                return when(viewType){
                    Normal_View_Type ->{
                        val view = layoutInflater.inflate(R.layout.list_item, parent, false)
                        return UserViewHolder(view)
                    }
                    else ->{
                        val view = layoutInflater.inflate(R.layout.empty_layout, parent, false)
                        return EmptyViewHolder(view)
                    }
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if(holder is UserViewHolder){
                    holder.bind(user = model){ name: String, photo: String, id: String ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra(UID, id)
                        intent.putExtra(NAME, name)
                        intent.putExtra(PHOTO, photo)
                        startActivity(intent)
                    }
                }
                else{
                    //Todo - something
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if(auth.uid == item!!.uid){
                    Deleted_View_Type
                }else{
                    Normal_View_Type
                }
            }
            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
            }

            override fun onError(e: Exception) {
                super.onError(e)
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}