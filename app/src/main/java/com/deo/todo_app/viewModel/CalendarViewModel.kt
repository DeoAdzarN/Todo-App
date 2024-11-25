package com.deo.todo_app.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.model.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    //Calendar
    private val _tasksWithDate = MutableLiveData<Map<String, List<Task>>>()
    val tasksWithDate: LiveData<Map<String, List<Task>>> get() = _tasksWithDate

    private val _tasksByDate = MutableLiveData<List<Task>>()
    val tasksByDate: LiveData<List<Task>> get() = _tasksByDate

    fun getTasksByDate(startMilis: Long,endMilis: Long) {
        viewModelScope.launch {
            taskRepository.getTaskByDate(startMilis, endMilis).observeForever { tasks ->
                _tasksByDate.postValue(tasks)
            }
        }
    }

    fun loadTask(startMilis:Long, endMilis:Long){
        viewModelScope.launch {
            val allTask = taskRepository.getTaskByMonth(startMilis, endMilis)
            val tasksMap = mutableMapOf<String, List<Task>>()

            allTask.forEach { tasks ->
                val formattedDate = SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(Date(tasks.date))
                tasksMap[formattedDate] = tasksMap.getOrDefault(formattedDate, emptyList()) + tasks
            }
            _tasksWithDate.value = tasksMap
        }
    }
}