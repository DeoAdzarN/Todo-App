package com.deo.todo_app.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.impl.background.systemalarm.RescheduleReceiver
import com.deo.todo_app.R
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.databinding.ActivityMainBinding
import com.deo.todo_app.utils.NotificationBarHelper
import com.deo.todo_app.utils.TaskReminders
import com.deo.todo_app.view.adapter.MainPagerAdapter
import com.deo.todo_app.viewModel.AuthViewModel
import com.deo.todo_app.viewModel.factory.AuthViewModelFactory
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        NotificationBarHelper().setStatusBarColorAndMode(this, R.color.cream, false)
        setContentView(_binding.root)

        val adapter = MainPagerAdapter(this)
        _binding.apply {
            container.apply {
                this.adapter = adapter
                offscreenPageLimit = 4
                isUserInputEnabled = false
                setOnScrollChangeListener(object : View.OnScrollChangeListener{
                    override fun onScrollChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int) {
                        TODO("Not yet implemented")
                    }

                })
            }

            bottomNav.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener{
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    when(item.itemId){
                        R.id.homeFragment -> {
                            container.currentItem = 0
                            return true
                        }
                        R.id.taskFragment -> {
                            container.currentItem = 1
                            return true
                        }
                        R.id.galleryFragment -> {
                            container.currentItem = 2
                            return true
                        }
                        R.id.profileFragment -> {
                            container.currentItem = 3
                            return true
                        }
                        else -> {
                            container.currentItem = 0
                            return true
                        }
                    }
                }

            })
        }
        checkAndRequestPermissions()
        checkNotificationPermission()

    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }else if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    showPermissionRationaleDialog()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val permissionsToRequest = mutableListOf<String>()
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                }
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                }
                if (permissionsToRequest.isNotEmpty()) {
                    ActivityCompat.requestPermissions(
                        this,
                        permissionsToRequest.toTypedArray(),
                        101
                    )
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        101
                    )
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        101
                    )
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(applicationContext)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs storage permission to pick and save images. Please enable the permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val packageName = packageName
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