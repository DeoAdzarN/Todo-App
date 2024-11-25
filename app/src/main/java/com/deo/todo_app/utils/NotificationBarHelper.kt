package com.deo.todo_app.utils

import android.app.Activity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat

class NotificationBarHelper {

    fun setStatusBarColorAndMode(activity: Activity, colorResId: Int, lightMode: Boolean){
        val window: Window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, colorResId)

        if (lightMode){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.decorView.systemUiVisibility = 0
        }
    }

}