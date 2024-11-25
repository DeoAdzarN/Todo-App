package com.deo.todo_app.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.viewModel.AttachmentViewModel
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.GalleryViewModel

class GalleryViewModelFactory(private val galleryRepository: GalleryRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(galleryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}