package com.deo.todo_app.viewModel

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

    private fun updateUserLocal(user:User){
        viewModelScope.launch {
            userRepository.updateUserLocal(user)
        }
    }

}