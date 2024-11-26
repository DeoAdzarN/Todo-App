package com.deo.todo_app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.deo.todo_app.R
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "sync_worker_channel"
        private const val NOTIFICATION_ID = 1
    }
    override suspend fun doWork(): Result {

        setForeground(createForegroundInfo())
        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, "Syncing your data ...", Toast.LENGTH_SHORT).show()
        }
        Log.e("syncWorker", "doWork: start" )
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

                // Wait until all the sync tasks are completed
                awaitAll(userSync, taskSync, attachmentSync, gallerySync)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Syncing Done", Toast.LENGTH_SHORT).show()
            }
            Log.e("syncWorker", "doWork: success" )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("syncWorker", "doWork: failure" )
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Failed to sync data", Toast.LENGTH_SHORT).show()
            }
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

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = createNotification()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val channelId = createNotificationChannel()
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Synchronizing with Server")
            .setContentText("Your data is being synchronized...")
            .setSmallIcon(R.drawable.ic_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel(): String {
        val channelId = NOTIFICATION_CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Sync Worker"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }
}