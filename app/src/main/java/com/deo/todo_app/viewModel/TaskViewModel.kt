package com.deo.todo_app.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(private val taskRepository: TaskRepository): ViewModel() {
    val pendingTasks = MutableLiveData<List<Task>>()
    val inProgressTasks = MutableLiveData<List<Task>>()
    val completedTasks = MutableLiveData<List<Task>>()

    val pendingCountTasks = MutableLiveData<Int>()
    val inProgressCountTasks = MutableLiveData<Int>()
    val completedCountTasks = MutableLiveData<Int>()

    val allTaskByStatus = MutableLiveData<List<Task>>()

    private val _taskCountThisMonth = MutableStateFlow<Map<String, Int>>(emptyMap())
    val taskCountThisMonth: StateFlow<Map<String, Int>> = _taskCountThisMonth

    fun getTaskCountByDateAndStatus(statuses: List<String>) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            taskRepository.getCountByDateAndStatus(startOfMonth, endOfMonth, "Pending").observeForever {
                pendingCountTasks.value = it
            }
            taskRepository.getCountByDateAndStatus(startOfMonth, endOfMonth, "On Progress").observeForever {
                inProgressCountTasks.value = it
            }
            taskRepository.getCountByDateAndStatus(startOfMonth, endOfMonth, "Completed").observeForever {
                completedCountTasks.value = it
            }
        }
    }

    fun getLimitTasksByStatus() {
        taskRepository.getLimitTaskByStatus("Pending",3).observeForever {
            pendingTasks.value = it
        }
        taskRepository.getLimitTaskByStatus("On Progress",3).observeForever {
            inProgressTasks.value = it
        }
        taskRepository.getLimitTaskByStatus("Completed",3).observeForever {
            completedTasks.value = it
        }
    }

    fun getTaskByStatus(status: String){
        taskRepository.getAllTaskByStatus(status).observeForever {
            allTaskByStatus.value = it
        }
    }

    fun fetchTasksAndSaveToRoom() {
        taskRepository.fetchTasksAndSaveToRoom()
    }

    suspend fun getOngoingTask() : List<Task>{
        return taskRepository.getOnGoingTask()
    }

    fun updateTask(task: Task,onResult: (userId:String, taskId:String) -> Unit){
        viewModelScope.launch {
            taskRepository.updateTask(task,onResult)
            getLimitTasksByStatus()
        }
    }

    fun insertTask(task: Task,onResult: (userId:String, taskId:String) -> Unit){
        viewModelScope.launch {
            taskRepository.insertTask(task,onResult)
        }
    }

    fun parseAttachments(json: String): List<Attachment> {
        return taskRepository.parseAttachments(json)
    }

    fun toJson(attachments: List<Attachment>): String {
        return taskRepository.toJson(attachments)
    }

}