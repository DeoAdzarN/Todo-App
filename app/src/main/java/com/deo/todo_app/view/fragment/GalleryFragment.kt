package com.deo.todo_app.view.fragment

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.GalleryRepository
import com.deo.todo_app.databinding.FragmentGalleryBinding
import com.deo.todo_app.helper.FirebaseStorageHelper
import com.deo.todo_app.model.Gallery
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
                if (checkAndRequestPermissions()) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                    }
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                }
            }

        }


        return view
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            progressDialog.show(parentFragmentManager, "progressDialog")
            val uri = data.data
            if (uri != null) {
                selectedImageUri = uri
                selectedImageUri?.let { uri ->
                    val type = getTypeFromUri(uri)
                    val localPath = getRealPathFromUri(uri)
                    val gallery = Gallery(
                        localPath = localPath,
                        firebaseUrl = uri.toString(),
                        type = if (type != null) {
                            if (type.startsWith("image/")) "image" else "video"
                        } else {
                            "image"
                        })
                    if (type != null) {
                        viewModel.uploadImage(gallery) { success, firebaseUrl ->
                            if (success && firebaseUrl != null) {
                                progressDialog.dismiss()
                                val image = Gallery(
                                    localPath = localPath,
                                    firebaseUrl = firebaseUrl,
                                    type = if (type.startsWith("image/")) "image" else "video"
                                )
                                viewModel.insertImage(image)
                                Toast.makeText(
                                    context,
                                    "Image uploaded successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                } ?: run {
                    Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readImagesPermission = context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }
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
            val readStoragePermission = context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
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

}