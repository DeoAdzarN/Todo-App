package com.deo.todo_app.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _sessionState = MutableLiveData<User?>()
    val sessionState: MutableLiveData<User?> = _sessionState

    private val _changePasswordResult = MutableLiveData<Result<Boolean>>()
    val changePasswordResult: MutableLiveData<Result<Boolean>> = _changePasswordResult

    fun checkSession(){
        viewModelScope.launch {
            val user = authRepository.checkSession()
            _sessionState.value = user
        }
    }

    fun login(email: String, password:String, onResult: (Boolean, String?) -> Unit){
        viewModelScope.launch {
            authRepository.login(email,password,onResult = { success, message ->
                onResult(success,message)
            })
        }
    }

    fun register(name:String, email:String, password:String, onResult: (Boolean, String?) -> Unit){
        viewModelScope.launch {
            authRepository.register(name,email,password,onResult = { success, message ->
                onResult(success,message)
            })
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            authRepository.logoutUser(context)
            _sessionState.value = null
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            authRepository.changePassword(currentPassword, newPassword){ success, message ->
                if (success) {
                    _changePasswordResult.value = Result.success(true)
                } else {
                    _changePasswordResult.value = Result.failure(Exception(message))
                }
            }
        }
    }

}