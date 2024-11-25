package com.deo.todo_app.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
        }
    }
}