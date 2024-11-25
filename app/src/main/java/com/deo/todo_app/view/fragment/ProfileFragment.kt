package com.deo.todo_app.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.databinding.FragmentProfileBinding
import com.deo.todo_app.view.activity.LoginActivity
import com.deo.todo_app.view.dialog.CustomDialog
import com.deo.todo_app.view.dialog.CustomDialog.showCustomChangePasswordDialog
import com.deo.todo_app.view.dialog.CustomDialog.showCustomEditNameDialog
import com.deo.todo_app.view.dialog.CustomDialog.showCustomLogoutDialog
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.UserViewModel
import com.deo.todo_app.viewModel.factory.AuthViewModelFactory
import com.deo.todo_app.viewModel.factory.UserViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel
    private var name: String? = null
    private val progressDialog = CustomDialog.ProgressDialogFragment()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = _binding.root
        context?.let {
            val authRepository = AuthRepository(
                auth = Firebase.auth,
                userRepository = UserRepository(
                    userDao = AppDatabase.getInstance(it).userDao(),
                    auth = Firebase.auth
                )
            )
            val userRepository = UserRepository(
                userDao = AppDatabase.getInstance(it).userDao(),
                auth = Firebase.auth
            )
            val userFactory = UserViewModelFactory(userRepository)
            val factory = AuthViewModelFactory(authRepository)
            userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]
            authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
            userViewModel.userData.observe(viewLifecycleOwner) { user ->
                name = user?.name
                _binding.greeting.text = "Halo, ${user?.name} !"
            }
            _binding.logout.setOnClickListener {
                showCustomLogoutDialog(requireContext()) { isLogout ->
                    if (isLogout) {
                        activity?.let { activities ->
                            authViewModel.logout(it.context)
                            val intent = Intent(activities, LoginActivity::class.java)
                            WorkManager.getInstance(activities.applicationContext).cancelAllWork()
                            startActivity(intent)
                            activities.finish()
                        }
                    }
                }

            }

            _binding.changeName.setOnClickListener {
                showCustomEditNameDialog(requireContext(), name ?: "") { newName ->
                    progressDialog.show(parentFragmentManager, "progressDialog")
                    val updateUser = userViewModel.userData.value?.copy(name = newName)
                    if (updateUser != null) {
                        userViewModel.updateUser(updateUser, onResult = { success, message ->
                            if (success) {
                                name = newName
                                _binding.greeting.text = "Halo, $newName !"
                                Toast.makeText(context, "Update Successfully", Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                            } else {
                                Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                            }
                        })
                    }
                }
            }

            _binding.changePassword.setOnClickListener {
                showCustomChangePasswordDialog(requireContext()) { currentPassword, newPassword ->
                    progressDialog.show(parentFragmentManager, "progressDialog")
                    authViewModel.updatePassword(currentPassword, newPassword)
                }
            }

            authViewModel.changePasswordResult.observe(viewLifecycleOwner) { result ->
                result.onSuccess {
                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }.onFailure { exception ->
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
        }


        return view
    }

    override fun onResume() {
        super.onResume()
        userViewModel.getUserData()
    }
}