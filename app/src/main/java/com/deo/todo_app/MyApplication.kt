package com.deo.todo_app

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.helper.FirebaseSyncHelper
import com.deo.todo_app.utils.SyncWorker
import com.deo.todo_app.viewModel.AttachmentViewModel
import com.deo.todo_app.viewModel.GalleryViewModel
import com.deo.todo_app.viewModel.TaskViewModel
import com.deo.todo_app.viewModel.UserViewModel
import com.deo.todo_app.viewModel.factory.TaskViewModelFactory
import com.deo.todo_app.viewModel.factory.UserViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        val networkHelper = FirebaseSyncHelper.getInstance(applicationContext)

        networkHelper.observeConnectionChanges { isConnected ->
            if (isConnected) {
                Log.e("syncWorker", "isConnected: success" )
                syncData()
            }else{
                Log.e("syncWorker", "isConnected: failure" )
            }
        }

    }

    private fun syncData() {
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
        Log.e("syncWorker", "worker enqueue" )
    }
}