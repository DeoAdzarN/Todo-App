package com.deo.todo_app.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.deo.todo_app.R
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.databinding.FragmentProfileBinding
import com.deo.todo_app.model.Gallery
import com.deo.todo_app.utils.Connectivity.isInternetAvailable
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
import java.io.File
import java.io.IOException
import java.io.InputStream

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
                Log.e("userData", "onCreateView: ${user?.pictureUrl}, ${user?.picturePath}" )
                val file = copyFileFromUri(requireContext(), Uri.parse(user?.picturePath))

                if (isInternetAvailable(context as Activity)){
                    Glide.with(it)
                        .load(if(user?.pictureUrl.isNullOrEmpty()) file else user?.pictureUrl)
                        .placeholder(R.drawable.default_profile_picture)
                        .into(_binding.picture)
                }else {
                    file.let { files ->
                        Glide.with(requireContext())
                            .load(files)
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.background_placeholder)
                            .into(_binding.picture)
                    }

                }
                _binding.greeting.text = "Halo, ${user?.name} !"

            }

            _binding.editPicture.setOnClickListener {
                if (checkAndRequestPermissions()){
                    openGallery()
                }
            }

            _binding.logout.setOnClickListener {
                showCustomLogoutDialog(requireContext()) { isLogout ->
                    if (isLogout) {
                        activity?.let { activities ->
                            if (isInternetAvailable(activities)) {
                                authViewModel.logout(it.context)
                            }else{
                                val update = userViewModel.userData.value?.copy(isLoggedIn = false)
                                if (update!=null) {
                                    userViewModel.updateUserLocal(update)
                                }
                            }
                            val intent = Intent(activities, LoginActivity::class.java)
                            WorkManager.getInstance(activities.applicationContext)
                                .cancelAllWork()
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
                        if (isInternetAvailable(activity = requireActivity())) {
                            val updateUserOnline = updateUser.copy(isSynced = true)
                            userViewModel.updateUser(updateUserOnline, onResult = { success, message ->
                                if (success) {
                                    name = newName
                                    _binding.greeting.text = "Halo, $newName !"
                                    Toast.makeText(
                                        context,
                                        "Update Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressDialog.dismiss()
                                } else {
                                    Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT)
                                        .show()
                                    progressDialog.dismiss()
                                }
                            })
                        }else{
                            val updateUserOffline = updateUser.copy(isSynced = false)
                            userViewModel.updateUserLocal(updateUserOffline)
                            Toast.makeText(
                                context,
                                "Update Successfully, need internet to sync with server",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            _binding.changePassword.setOnClickListener {
                showCustomChangePasswordDialog(requireContext()) { currentPassword, newPassword ->
                    progressDialog.show(parentFragmentManager, "progressDialog")
                    if (isInternetAvailable(activity = requireActivity())) {
                        authViewModel.updatePassword(currentPassword, newPassword)
                    }else{
                        Toast.makeText(
                            context,
                            "Need internet to change password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var realPath: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                val index = cursor.getColumnIndex("_data")
                if (index != -1 && cursor.moveToFirst()) {
                    realPath = cursor.getString(index)
                }
                cursor.close()
            }
        } else if (uri.scheme == "file") {
            realPath = uri.path
        }
        return realPath
    }
    @SuppressLint("IntentReset")
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickMediaLauncher.launch(intent)
    }
    private fun copyFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                progressDialog.show(parentFragmentManager, "progressDialog")
                uri.let { _uri ->
                    try {
                        activity?.let {
                            if (isInternetAvailable(it)){
                                userViewModel.updatePicture(_uri, onResult = { success, message ->
                                    if (success) {
                                        Log.d("GlideDebug", "URI: $_uri")
                                        Glide.with(it).load(_uri)
                                            .placeholder(R.drawable.default_profile_picture)
                                            .into(_binding.picture)
                                        Toast.makeText(
                                            context,
                                            "Update Successfully",
                                            Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                })
                            }else{
                                val update = userViewModel.userData.value?.copy(picturePath = _uri.toString(), pictureUrl = "", isSynced = false)
                                if (update!=null) {
                                    userViewModel.updateUserLocal(update)
                                    val file = copyFileFromUri(requireContext(), Uri.parse(update.picturePath))
                                    Log.d("GlideDebug", "URI: $_uri, Path : ${file?.path.toString()}")
                                    file?.let { files->
                                        Glide.with(requireContext())
                                            .load(files)
                                            .placeholder(R.drawable.default_profile_picture)
                                            .error(R.drawable.background_placeholder)
                                            .into(_binding.picture)
                                    }
                                }
                                Toast.makeText(
                                    context,
                                    "Update Successfully, need internet to sync with server",
                                    Toast.LENGTH_SHORT).show()
                            }
                            progressDialog.dismiss()
                        }
                    } catch (e: IOException) {
                        Log.e("ImagePicker", "Error processing URI: ${e.message}")
                        Toast.makeText(context, "Failed to process image URI", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(context, "No media selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readImagesPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            )
            if (readImagesPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    101
                )
                false
            } else {
                true
            }
        } else {
            val readStoragePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
                false
            } else {
                true
            }
        }
    }
    @SuppressLint("IntentReset")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permissions[0])) {
                    showPermissionRationaleDialog()
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(context)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs storage permission to pick and save images. Please enable the permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val packageName = context?.packageName
                val intent = Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    override fun onResume() {
        super.onResume()
        userViewModel.getUserData()
    }
}