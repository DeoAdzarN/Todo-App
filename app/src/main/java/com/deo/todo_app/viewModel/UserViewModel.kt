package com.deo.todo_app.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.model.User
import kotlinx.coroutines.launch

class UserViewModel(private  val userRepository: UserRepository) : ViewModel(){

    private val _userData = MutableLiveData<User?>()
    val userData: MutableLiveData<User?> = _userData

    fun getUserData(){
        viewModelScope.launch {
            val user = userRepository.getUserFromLocal()
            _userData.value = user
        }
    }

    fun updateUser(user: User, onResult: (Boolean, String?) -> Unit){
        viewModelScope.launch {
            userRepository.updateUser(user.name, onResult = { success, message ->
                if (success){
                    updateUserLocal(user)
                    onResult(true,null)
                }else{
                    onResult(false,message)
                }
            })
        }
    }

    fun updatePicture(uri: Uri, onResult: (Boolean, String?) -> Unit){
        viewModelScope.launch {
            userRepository.updateUserProfile(uri, onResult = { success, photoUrl ->
                if (success) {
                    val updatedUser = userData.value?.copy(picturePath = uri.toString(),pictureUrl = photoUrl?:"")
                    if (updatedUser != null)
                        updateUserLocal(updatedUser)
                    onResult(true, null)
                } else {
                    onResult(false, photoUrl)
                }
            })
        }
    }
    fun syncUserToFirestore() {
        viewModelScope.launch {
            userRepository.syncOfflineUser()
        }
    }
    fun updateUserLocal(user:User){
        viewModelScope.launch {
            userRepository.updateUserLocal(user)
        }
    }

}