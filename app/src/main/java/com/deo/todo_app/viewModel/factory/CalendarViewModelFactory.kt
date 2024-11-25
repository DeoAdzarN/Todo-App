package com.deo.todo_app.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.CalendarViewModel
import com.deo.todo_app.viewModel.TaskViewModel

class CalendarViewModelFactory(private val taskRepository: TaskRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}