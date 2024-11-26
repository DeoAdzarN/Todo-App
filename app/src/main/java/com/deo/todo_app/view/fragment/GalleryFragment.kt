package com.deo.todo_app.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.databinding.FragmentGalleryBinding
import com.deo.todo_app.helper.FirebaseStorageHelper
import com.deo.todo_app.model.Gallery
import com.deo.todo_app.utils.Connectivity.isInternetAvailable
import com.deo.todo_app.view.activity.PreviewVideoActivity
import com.deo.todo_app.view.adapter.GalleryAdapter
import com.deo.todo_app.view.dialog.CustomDialog
import com.deo.todo_app.view.dialog.CustomDialog.showImageDialog
import com.deo.todo_app.viewModel.GalleryViewModel
import com.deo.todo_app.viewModel.factory.GalleryViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class GalleryFragment : Fragment() {
    private lateinit var _binding: FragmentGalleryBinding
    private lateinit var viewModel: GalleryViewModel
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val progressDialog = CustomDialog.ProgressDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val view = _binding.root
        context?.let {
            val galleryRepository = GalleryRepository(
                galleryDao = AppDatabase.getInstance(it).galleryDao(),
                firebaseStorage = FirebaseStorage.getInstance(),
                firestore = FirebaseFirestore.getInstance()
            )
            val galleryFactory = GalleryViewModelFactory(galleryRepository)
            viewModel = ViewModelProvider(this, galleryFactory)[GalleryViewModel::class.java]

            viewModel.imagesLiveData.observe(viewLifecycleOwner) { galleryList ->
                if (galleryList.isEmpty()) {
                    _binding.rvGallery.visibility = View.GONE
                    _binding.emptyState.visibility = View.VISIBLE
                } else {
                    _binding.rvGallery.visibility = View.VISIBLE
                    _binding.emptyState.visibility = View.GONE
                }
                _binding.rvGallery.adapter = GalleryAdapter(it, galleryList) { it ->
                    //setOnClick
                    if (it.type == "image") {
                        showImageDialog(requireContext(),it.firebaseUrl,it.localPath)
                    }else{
                        val intent = Intent(requireContext(), PreviewVideoActivity::class.java)
                        intent.putExtra("videoUrl", it.firebaseUrl)
                        intent.putExtra("videoPath", it.localPath)
                        startActivity(intent)
                    }
                }
            }

            _binding.addImage.setOnClickListener {
                if (checkAndRequestPermissions()){
                    openGallery()
                }
            }

        }


        return view
    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        intent.type = "*/*"
        pickMediaLauncher.launch(intent)
    }

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                progressDialog.show(parentFragmentManager, "progressDialog")
                    selectedImageUri = uri
                    selectedImageUri?.let { uri ->
                        val type = getTypeFromUri(uri)
                        val localPath = getRealPathFromUri(uri)

                        if (type != null) {
                            val gallery = Gallery(
                                localPath = localPath,
                                firebaseUrl = uri.toString(),
                                type = if (type != null) {
                                    if (type.startsWith("image/")) "image" else "video"
                                } else {
                                    "image"
                                })
                            if (isInternetAvailable(requireActivity())) {
                                viewModel.uploadImage(gallery) { success, firebaseUrl ->
                                    if (success && firebaseUrl != null) {
                                        progressDialog.dismiss()
                                        viewModel.insertImage(gallery.copy(firebaseUrl = firebaseUrl, synced = true))
                                        Toast.makeText(
                                            context,
                                            "Image uploaded successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to upload image",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            }else{
                                progressDialog.dismiss()
                                viewModel.insertImage(gallery.copy(synced = false))
                            }
                        }

                    } ?: run {
                        Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "No media selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchAllImages()
    }

    private fun getTypeFromUri(uri: Uri): String? {
        return context?.contentResolver?.getType(uri)
    }
    private fun getRealPathFromUri(uri: Uri): String {
        var path = ""
        val cursor = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
            if (index != -1) {
                path = it.getString(index)
            }
            it.close()
        }
        return path
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
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
                intent.type = "*/*"
                pickMediaLauncher.launch(intent)
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

}