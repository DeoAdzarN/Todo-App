package com.deo.todo_app.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DialogViewModel: ViewModel() {
    private val _dialogInputName = MutableLiveData<String>()
    val dialogInputName: LiveData<String> = _dialogInputName

    fun onDialogSaveName(name:String) {
        _dialogInputName.value = name
    }
}