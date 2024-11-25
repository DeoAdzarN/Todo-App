package com.deo.todo_app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RebootReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action){

        }
    }
}