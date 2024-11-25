package com.deo.todo_app.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.deo.todo_app.R

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val taskTitle = inputData.getString("title") ?: "Remember your task"
        val taskDescription = inputData.getString("desc") ?: "Click here to open the app"
        Log.d("ReminderWorker", "Executing worker for task ID: $taskTitle")

        // Show Notification
        showNotification(taskTitle, taskDescription)
//        setForegroundAsync(createForegroundInfo())

        return Result.success()
    }
    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "Reminder_Service"
        val title = "Task Reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Task Reminder", NotificationManager.IMPORTANCE_LOW)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText("Running task reminder service")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .build()

        return ForegroundInfo(1, notification)
    }
    @SuppressLint("NotificationPermission")
    private fun showNotification(taskTitle:String, taskDesc:String){
        Log.d("AlarmWorker", "Showing notification for task ID: $taskTitle")
        val channelId = "Task_Reminder_Channel"
        val channelName= "Task Reminder"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(taskTitle)
            .setContentText(taskDesc)
            .setSmallIcon(R.drawable.logo_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(),notification)
    }
}