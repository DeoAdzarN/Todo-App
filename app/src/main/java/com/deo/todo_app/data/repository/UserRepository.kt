package com.deo.todo_app.data.repository

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.deo.todo_app.data.local.dao.UserDao
import com.deo.todo_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun updateUserProfile(
        uri: Uri,
        onResult: (Boolean, String?) -> Unit
    ) {
        updateFirebaseUserPicture(uri, onResult)
    }
    private fun updateFirebaseUserPicture(uri: Uri, onResult: (Boolean, String?) -> Unit) {
        val firebaseUser = auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            photoUri = uri
        }
        firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                onResult(true,firebaseUser.photoUrl.toString())
            }else{
                onResult(false,task.exception?.message)
            }
        }
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

    suspend fun syncOfflineUser() {
        val unSyncedUser = userDao.getUser()
        val firebaseUser = auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = unSyncedUser.name
            photoUri = Uri.parse(unSyncedUser.picturePath)
        }
        firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.updateUser(unSyncedUser.copy(isSynced = true,pictureUrl = firebaseUser.photoUrl.toString(),picturePath = unSyncedUser.picturePath))
                }
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