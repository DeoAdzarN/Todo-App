package com.deo.todo_app.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.deo.todo_app.data.local.dao.TaskDao
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Task
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskRepository(private val taskDao: TaskDao, private val firestore: FirebaseFirestore) {
    private val gson = Gson()

    suspend fun getAllTask(): List<Task> {
        return taskDao.getAllTasks()
    }

    suspend fun getOnGoingTask(): List<Task> {
        return taskDao.getOnGoingTasks()
    }

    suspend fun getTaskByMonth(startMilis: Long, endMilis:Long): List<Task>{
        return taskDao.getTasksInRange(startMilis, endMilis)
    }
    fun getTaskByDate(dateMilis: Long): LiveData<List<Task>>{
        return taskDao.getTasksByDate(dateMilis)
    }

    fun getLimitTaskByStatus(status:String, limit:Int): LiveData<List<Task>>{
        return taskDao.getLimitedTaskByStatus(status, limit)
    }

    fun getAllTaskByStatus(status: String): LiveData<List<Task>>{
        return taskDao.getAllTaskByStatus(status)
    }

    fun getTaskById(id: String): LiveData<Task>{
        return taskDao.getTaskById(id)
    }

    fun getCountByDateAndStatus(startOfMonth: Long, endOfMonth: Long, status: String): LiveData<Int> {
        return taskDao.getCountByDateAndStatus(startOfMonth, endOfMonth, status)
    }

    suspend fun insertTask(task: Task, onResult: (userId:String, taskId:String) -> Unit){
        taskDao.insertTask(task)
        syncTaskWithFirestore(task,onResult)
    }
    suspend fun updateTask(task: Task, onResult: (userId:String, taskId:String) -> Unit){
        Log.e("updateTask", "step 3")
        taskDao.updateTask(task)
        syncTaskWithFirestore(task, onResult)
    }

    private fun syncTaskWithFirestore(task: Task, onResult: (userId:String, taskId:String) -> Unit) {
        Log.e("updateTask", "step 4")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("tasks").document(task.id).set(task)
            .addOnSuccessListener {
                Log.e("updateTask", "step 5")
                onResult(userId,task.id)
                CoroutineScope(Dispatchers.IO).launch {
                    taskDao.updateTask(task.copy(synced = true))
                    firestore.collection("users").document(userId)
                    .collection("tasks").document(task.id).update("synced", true)
                }
            }
    }

    suspend fun syncOfflineTasks(context:Context) {
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        unsyncedTasks.forEach {
            syncTaskWithFirestore(it, onResult = { userId, taskId ->
            })
            taskDao.insertTask(it.copy(synced = true))
        }
    }

    fun fetchTasksAndSaveToRoom() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("tasks")
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.mapNotNull { document ->
                    val task = document.toObject(Task::class.java)
                    task.copy(synced = true)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    taskDao.insertAll(tasks)

                }
            }
            .addOnFailureListener { exception ->
                Log.e("fetchTasksAndSaveToRoom", "Error fetching tasks: ${exception.message}")
            }
    }

    fun parseAttachments(json: String): List<Attachment> {
        return gson.fromJson(json, object : TypeToken<List<Attachment>>() {}.type)
    }

    fun toJson(attachments: List<Attachment>): String {
        return gson.toJson(attachments)
    }
}