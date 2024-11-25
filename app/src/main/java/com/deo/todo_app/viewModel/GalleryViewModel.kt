package com.deo.todo_app.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.model.Gallery
import kotlinx.coroutines.launch

class GalleryViewModel(private val galleryRepository: GalleryRepository) : ViewModel() {

    private val _imagesLiveData = MutableLiveData<List<Gallery>>()
    val imagesLiveData: LiveData<List<Gallery>> get() = _imagesLiveData

    fun fetchAllImages() {
        viewModelScope.launch {
            val gallery = galleryRepository.getAllImages()
            _imagesLiveData.postValue(gallery)
        }
    }

    fun insertImage(gallery: Gallery) {
        viewModelScope.launch {
            galleryRepository.insertImage(gallery)
            fetchAllImages()
        }
    }

    fun uploadImage(gallery: Gallery, onComplete: (Boolean, String?) -> Unit) {
        galleryRepository.uploadMediaToFirebase(gallery, onComplete)
    }
    fun fetchGalleryAndSaveToRoom() {
        galleryRepository.syncMediaFromFirestore()
    }

    fun syncGalleryToFirestore() {
        viewModelScope.launch {
            galleryRepository.syncOfflineGallery()
        }
    }
}