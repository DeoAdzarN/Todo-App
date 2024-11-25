package com.deo.todo_app.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.deo.todo_app.R
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.utils.NotificationBarHelper
import com.deo.todo_app.databinding.ActivityLoginBinding
import com.deo.todo_app.utils.TaskReminders
import com.deo.todo_app.view.dialog.CustomDialog
import com.deo.todo_app.viewModel.AttachmentViewModel
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.GalleryViewModel
import com.deo.todo_app.viewModel.TaskViewModel
import com.deo.todo_app.viewModel.factory.AttachmentViewModelFactory
import com.deo.todo_app.viewModel.factory.AuthViewModelFactory
import com.deo.todo_app.viewModel.factory.GalleryViewModelFactory
import com.deo.todo_app.viewModel.factory.TaskViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var attachmentViewModel: AttachmentViewModel
    private val progressDialog = CustomDialog.ProgressDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        NotificationBarHelper().setStatusBarColorAndMode(this, R.color.cream, false)
        setContentView(_binding.root)
        val authRepository = AuthRepository(
            auth = Firebase.auth,
            userRepository = UserRepository(
                userDao = AppDatabase.getInstance(applicationContext).userDao(),
                auth = Firebase.auth
            )
        )
        val taskRepository = TaskRepository(
            taskDao = AppDatabase.getInstance(applicationContext).taskDao(),
            firestore = Firebase.firestore
        )
        val galleryRepository = GalleryRepository(
            galleryDao = AppDatabase.getInstance(applicationContext).galleryDao(),
            firebaseStorage = Firebase.storage,
            firestore = Firebase.firestore
        )
        val attachmentRepository = AttachmentRepository(
            attachmentDao = AppDatabase.getInstance(applicationContext).attachmentDao(),
            firestore = Firebase.firestore
        )
        val factory = AuthViewModelFactory(authRepository)
        val taskFactory = TaskViewModelFactory(taskRepository)
        val galleryFactory = GalleryViewModelFactory(galleryRepository)
        val attachmentFactory = AttachmentViewModelFactory(attachmentRepository)
        attachmentViewModel = ViewModelProvider(this,attachmentFactory)[AttachmentViewModel::class.java]
        taskViewModel = ViewModelProvider(this, taskFactory)[TaskViewModel::class.java]
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        galleryViewModel = ViewModelProvider(this, galleryFactory)[GalleryViewModel::class.java]
        _binding.apply {
            entryPointContainer.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
            visibilityPassword.setOnClickListener {
                toggleVisibilityPassword(inputPassword, visibilityPassword)
            }
            loginBtn.setOnClickListener {
                progressDialog.show(supportFragmentManager, "loading")
                if (validate()){
                    authViewModel.login(
                        email = inputEmail.text.toString(),
                        password = inputPassword.text.toString(),
                        onResult = { success, message ->
                            if (success){
                                progressDialog.dismiss()
                                Toast.makeText(applicationContext, "Login successfully", Toast.LENGTH_SHORT).show()
                                taskViewModel.fetchTasksAndSaveToRoom()
                                galleryViewModel.fetchGalleryAndSaveToRoom()
                                attachmentViewModel.fetchMediaAndSaveToRoom()
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val tasks = taskViewModel.getOngoingTask()
                                    tasks.forEach { task ->
                                        TaskReminders.scheduleReminder(applicationContext,task)
                                    }
                                }
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }else{
                                progressDialog.dismiss()
                                Toast.makeText(applicationContext, message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }else{
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun validate():Boolean {
        var isValid: Boolean
        _binding.apply {
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