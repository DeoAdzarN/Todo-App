package com.deo.todo_app.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.deo.todo_app.R
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.databinding.ActivityViewAllTaskBinding
import com.deo.todo_app.utils.NotificationBarHelper
import com.deo.todo_app.view.adapter.HomeChildAdapter
import com.deo.todo_app.viewModel.TaskViewModel
import com.deo.todo_app.viewModel.factory.TaskViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ViewAllTaskActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityViewAllTaskBinding
    private lateinit var status: String
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityViewAllTaskBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        if (intent.hasExtra("status")) {
            status = intent.getStringExtra("status").toString()
            _binding.title.text = status
            when (status) {
                "Pending" -> {
                    _binding.actionBar.setBackgroundColor(getColor(R.color.yellow))
                    NotificationBarHelper().setStatusBarColorAndMode(this, R.color.yellow,true)
                }

                "On Progress" -> {
                    _binding.actionBar.setBackgroundColor(getColor(R.color.light_blue))
                    NotificationBarHelper().setStatusBarColorAndMode(this, R.color.light_blue,true)
                }

                "Completed" -> {
                    _binding.actionBar.setBackgroundColor(getColor(R.color.green))
                    NotificationBarHelper().setStatusBarColorAndMode(this, R.color.green,true)
                }
            }
            initViewModel()
            callData()
            observeData()
            _binding.actionBack.setOnClickListener {
                finish()
            }
        }else{
            finish()
        }

    }

    private fun observeData() {
        taskViewModel.allTaskByStatus.observe(this) { tasks ->
            val childAdapter = HomeChildAdapter(supportFragmentManager,tasks)
            _binding.rvTask.adapter = childAdapter
            _binding.rvTask.layoutManager = LinearLayoutManager(this)

            if (tasks.isEmpty()) {
                _binding.emptyText.visibility = View.VISIBLE
                _binding.rvTask.visibility = View.GONE
            } else {
                _binding.emptyText.visibility = View.GONE
                _binding.rvTask.visibility = View.VISIBLE
            }
        }
    }

    private fun callData() {
        taskViewModel.getTaskByStatus(status)

    }

    private fun initViewModel() {
        val taskRepository = TaskRepository(
            taskDao = AppDatabase.getInstance(applicationContext).taskDao(),
            firestore = Firebase.firestore
        )
        val taskFactory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, taskFactory)[TaskViewModel::class.java]
    }
}