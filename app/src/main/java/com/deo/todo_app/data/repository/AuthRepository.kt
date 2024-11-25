package com.deo.todo_app.data.repository

import android.content.Context
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepository(private val auth:FirebaseAuth,private val userRepository: UserRepository) {

    fun login(email:String, password:String, onResult: (Boolean, String?) -> Unit){

        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val user = auth.currentUser
                    user?.let {
                        val localUser = User(
                            userId = it.uid,
                            name = it.displayName ?: "",
                            email = it.email ?: "",
                            isLoggedIn = true
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            userRepository.saveUserToLocal(localUser)
                        }
                        onResult(true, null)
                    }
                }else{
                    onResult(false,task.exception?.message)
                }

            }

    }

    fun register(name: String, email: String, password:String,onResult: (Boolean, String?) -> Unit){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user?.let {
                    it.updateProfile(profileUpdates)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                val localUser = User(
                                    userId = it.uid,
                                    name = it.displayName ?: "",
                                    email = it.email ?: "",
                                    isLoggedIn = true
                                )
                                CoroutineScope(Dispatchers.IO).launch {
                                    userRepository.saveUserToLocal(localUser)
                                }
                                onResult(true,null)
                            }else{
                                onResult(false,updateTask.exception?.message)
                            }
                        }
                }

            }else{
                onResult(false,task.exception?.message)
            }
        }
    }

    suspend fun checkSession(): User {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            // Online session
            val user = User(
                userId = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                isLoggedIn = true
            )
            userRepository.saveUserToLocal(user)
            user
        } else {
            userRepository.getUserFromLocal()
        }
    }

    suspend fun logoutUser(context: Context) {
        auth.signOut()
        userRepository.clearUser()
        AppDatabase.deleteDatabase(context)
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit){
        val user = auth.currentUser
        if (user!=null){
            val credential = EmailAuthProvider.getCredential(user.email!!,currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful){
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            onResult(true, null)
                        }else{
                            onResult(false,updateTask.exception?.message)
                        }
                    }
                }else{
                    onResult(false,reauthTask.exception?.message)
                }
            }
        } else {
            onResult(false, "User not authenticated")
        }
    }

}