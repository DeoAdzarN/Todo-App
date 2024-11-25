package com.deo.todo_app.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.deo.todo_app.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseSyncHelper(private val repository: TaskRepository): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (isNetworkAvailable(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.syncOfflineTasks(context)
            }
        }
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}