package com.deo.todo_app.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.TaskViewModel
import com.deo.todo_app.viewModel.factory.AuthViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        val authRepository = AuthRepository(
            auth = Firebase.auth,
            userRepository = UserRepository(
                userDao = AppDatabase.getInstance(applicationContext).userDao(),
                auth = Firebase.auth
            )
        )
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        authViewModel.checkSession()

        authViewModel.sessionState.observe(this) { user ->
            if (user != null ){
                Log.e("sessionCheck", "user not null" )
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                Log.e("sessionCheck", "user null" )
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
    }
}