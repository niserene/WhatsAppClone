package com.nishantdev961.whatsappclone.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nishantdev961.whatsappclone.fragments.InboxFragment
import com.nishantdev961.whatsappclone.fragments.PeopleFragment

class ScreenSliderAdapter(fa : FragmentActivity) : FragmentStateAdapter(fa){

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment  = when(position){

        0-> InboxFragment()
        else -> PeopleFragment()
    }

}
