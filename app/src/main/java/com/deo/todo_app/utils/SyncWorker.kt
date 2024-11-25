package com.deo.todo_app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.utils.Connectivity.isInternetAvailable
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!isNetworkAvailable(applicationContext)) {
            Log.e("syncWorker","SyncWorker: No network available, retrying later")
            return Result.retry()
        }


        return try {
            val userRepository = UserRepository(
                userDao = AppDatabase.getInstance(applicationContext).userDao(),
                auth = Firebase.auth
            )
            val taskRepository = TaskRepository(
                taskDao = AppDatabase.getInstance(applicationContext).taskDao(),
                firestore = Firebase.firestore
            )
            val attachmentRepository = AttachmentRepository(
                attachmentDao = AppDatabase.getInstance(applicationContext).attachmentDao(),
                firestore = Firebase.firestore
            )
            val galleryRepository = GalleryRepository(
                galleryDao = AppDatabase.getInstance(applicationContext).galleryDao(),
                firebaseStorage = Firebase.storage,
                firestore = Firebase.firestore
            )

            coroutineScope {
                val userSync = async { userRepository.syncOfflineUser() }
                val taskSync = async { taskRepository.syncOfflineTasks() }
                val attachmentSync = async { attachmentRepository.syncOfflineAttach() }
                val gallerySync = async { galleryRepository.syncOfflineGallery() }

                userSync.await()
                taskSync.await()
                attachmentSync.await()
                gallerySync.await()
            }

            Log.e("syncWorker", "doWork: success" )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("syncWorker", "doWork: failure" )
            Result.failure()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}