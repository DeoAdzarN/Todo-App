package com.deo.todo_app.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.deo.todo_app.R
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.utils.NotificationBarHelper
import com.deo.todo_app.databinding.ActivityRegisterBinding
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.factory.AuthViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        NotificationBarHelper().setStatusBarColorAndMode(this, R.color.cream, false)
        setContentView(_binding.root)
        val authRepository = AuthRepository(
            auth = Firebase.auth,
            userRepository = UserRepository(
                userDao = AppDatabase.getInstance(applicationContext).userDao(),
                auth = Firebase.auth
            )
        )
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        _binding.apply {
            entryPointContainer.setOnClickListener {
                finish()
            }

            visibilityPassword.setOnClickListener {
                toggleVisibilityPassword(inputPassword, visibilityPassword)
            }
            visibilityConfirmPassword.setOnClickListener {
                toggleVisibilityPassword(inputConfirmPassword, visibilityConfirmPassword)
            }

            registerBtn.setOnClickListener {
                if(validate()){
                    authViewModel.register(
                        name = inputName.text.toString(),
                        email = inputEmail.text.toString(),
                        password = inputPassword.text.toString(),
                        onResult = { success, message ->
                            if (success){
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                                Toast.makeText(applicationContext, "Register successfully", Toast.LENGTH_SHORT).show()
                            }else{
                                Log.e("register", "onCreate: $message", )
                                Toast.makeText(applicationContext, message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun validate():Boolean {
        var isValid: Boolean
        _binding.apply {
            if(inputName.text.isNullOrEmpty()){
                nameError.visibility = View.VISIBLE
                isValid = false
            }else{
                nameError.visibility = View.GONE
                isValid = true
            }
            if(inputEmail.text.isNullOrEmpty()){
                emailError.visibility = View.VISIBLE
                emailError.text = getString(R.string.email_cannot_empty)
                isValid = false
            }else{
                if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()){
                    emailError.visibility = View.VISIBLE
                    emailError.text = getString(R.string.format_email_error)
                    isValid = false
                }else{
                    emailError.visibility = View.GONE
                    isValid = true
                }
            }

            if(inputPassword.text.isNullOrEmpty()){
                passwordError.visibility = View.VISIBLE
                isValid = false
            }else{
                passwordError.visibility = View.GONE
                isValid = true
            }
            if(inputConfirmPassword.text.isNullOrEmpty()){
                confirmPasswordError.visibility = View.VISIBLE
                confirmPasswordError.text = getString(R.string.password_cannot_empty)
                isValid = false
            }else{
                confirmPasswordError.visibility = View.GONE
                if (inputPassword.text.toString() != inputConfirmPassword.text.toString()){
                    confirmPasswordError.visibility = View.VISIBLE
                    confirmPasswordError.text = getString(R.string.confirm_password_diff)
                    isValid = false
                }else{
                    confirmPasswordError.visibility = View.GONE
                    isValid = true
                }
            }

            return isValid
        }

    }

    private fun toggleVisibilityPassword(passwordEditText: EditText, toggleIcon: ImageView) {
        val currentTypeface = passwordEditText.typeface

        if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            toggleIcon.setImageResource(R.drawable.ic_visibility_off)
        }else{
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_visible_on)
        }

        passwordEditText.typeface = currentTypeface
        passwordEditText.setSelection(passwordEditText.text.length)
    }
}