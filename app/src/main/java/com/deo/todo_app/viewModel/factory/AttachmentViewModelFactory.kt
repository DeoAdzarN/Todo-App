package com.deo.todo_app.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.viewModel.AttachmentViewModel
import com.deo.todo_app.viewModel.AuthViewModel

class AttachmentViewModelFactory(private val attachmentRepository: AttachmentRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttachmentViewModel::class.java)) {
            return AttachmentViewModel(attachmentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}