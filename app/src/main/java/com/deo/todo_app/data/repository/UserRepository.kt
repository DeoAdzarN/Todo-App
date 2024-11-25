package com.deo.todo_app.data.repository

import android.widget.Toast
import androidx.lifecycle.LiveData
import com.deo.todo_app.data.local.dao.UserDao
import com.deo.todo_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest

class UserRepository(private val userDao: UserDao, private val auth: FirebaseAuth) {


    suspend fun saveUserToLocal(user:User){
        userDao.insertUser(user)
    }

    suspend fun getUserFromLocal():User{
        return userDao.getUser()
    }

    suspend fun updateUserLocal(user: User){
        userDao.updateUser(user)
    }

    fun updateUser(
        name:String,
        onResult: (Boolean, String?) -> Unit
    ) {
        updateFirebaseUser(name, onResult)
    }

    private fun updateFirebaseUser(name: String, onResult: (Boolean, String?) -> Unit) {
        val firebaseUser = auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }
        firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                onResult(true,null)
            }else{
                onResult(false,task.exception?.message)
            }
        }
    }

    suspend fun clearUser(){
        userDao.clearUser()
    }

    fun getCurrentFirebaseUser():FirebaseUser?{
        return auth.currentUser
    }

}