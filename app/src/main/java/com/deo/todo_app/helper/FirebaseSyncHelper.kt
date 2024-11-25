package com.deo.todo_app.helper

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.deo.todo_app.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseSyncHelper private constructor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    companion object {
        private var instance: FirebaseSyncHelper? = null

        fun getInstance(context: Context): FirebaseSyncHelper {
            if (instance == null) {
                instance = FirebaseSyncHelper(context)
            }
            return instance!!
        }
    }

    fun observeConnectionChanges(onConnectionChange: (Boolean) -> Unit) {
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onConnectionChange(true)
            }

            override fun onLost(network: Network) {
                onConnectionChange(false)
            }
        })
    }
}