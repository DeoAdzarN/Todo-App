package com.deo.todo_app.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Task
import kotlinx.coroutines.launch

class AttachmentViewModel(private val attachmentRepository: AttachmentRepository) : ViewModel() {
    private val _attachment = MutableLiveData<List<Attachment>>()
    val attach: LiveData<List<Attachment>> = _attachment

    fun insertAttachment(attachments:List<Attachment>,onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            attachmentRepository.insertAttachment(attachments, onResult)
        }
    }
    fun fetchMediaAndSaveToRoom() {
        attachmentRepository.syncMediaFromFirestore()
    }
    fun getAllAttachment(){
        viewModelScope.launch {
            _attachment.postValue(attachmentRepository.getAllAttachment())
        }
    }

    fun removeAttachmentByTaskId(listAttachmentRemoveId: List<String>) {
        viewModelScope.launch {
            attachmentRepository.removeAttachmentByTaskId(listAttachmentRemoveId)
        }
    }

    fun getAttachmentsByTaskId(taskId: String){
       viewModelScope.launch {
           _attachment.postValue(attachmentRepository.getAttachmentsByTaskId(taskId))
       }
    }

    private val _mediaList = MutableLiveData<List<Attachment>>(emptyList())
    val mediaList: LiveData<List<Attachment>> get() = _mediaList

    fun addMedia(mediaItem: Attachment) {
        val currentList = _mediaList.value.orEmpty()
        _mediaList.value = currentList + mediaItem
    }

}