package com.deo.todo_app.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.deo.todo_app.view.fragment.GalleryFragment
import com.deo.todo_app.view.fragment.HomeFragment
import com.deo.todo_app.view.fragment.ProfileFragment
import com.deo.todo_app.view.fragment.TaskFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> TaskFragment()
            2 -> GalleryFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
    }

}