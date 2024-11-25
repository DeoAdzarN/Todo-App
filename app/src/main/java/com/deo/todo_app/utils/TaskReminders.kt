package com.deo.todo_app.utils

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.model.Task
import java.util.concurrent.TimeUnit

object TaskReminders {
    fun scheduleReminder(context: Context, task: Task) {
        val remindWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(calculateDelay(task.reminder), TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "taskId" to task.id,
                    "title" to task.title,
                    "desc" to task.description
                )
            )
            .build()

        // Work kedua untuk dateTime
        val dateTimeWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(calculateDelay(task.date), TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "taskId" to task.id,
                    "title" to task.title,
                    "desc" to task.description
                )
            )
            .build()

        val listWorkRequest = listOf(remindWorkRequest, dateTimeWorkRequest)

        WorkManager.getInstance(context).enqueueUniqueWork(
            "Reminder_${task.id}",
            ExistingWorkPolicy.REPLACE,
            listWorkRequest
        )
    }

    private fun calculateDelay(destinationTime: Long): Long {
        val currentTime = System.currentTimeMillis()
        return destinationTime - currentTime
    }
}