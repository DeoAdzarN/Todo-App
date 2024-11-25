package com.deo.todo_app.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AuthRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.data.repository.UserRepository
import com.deo.todo_app.databinding.FragmentHomeBinding
import com.deo.todo_app.model.Task
import com.deo.todo_app.view.adapter.HomeParentAdapter
import com.deo.todo_app.viewModel.TaskViewModel
import com.deo.todo_app.viewModel.UserViewModel
import com.deo.todo_app.viewModel.factory.AuthViewModelFactory
import com.deo.todo_app.viewModel.factory.TaskViewModelFactory
import com.deo.todo_app.viewModel.factory.UserViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private lateinit var _binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var taskViewModel: TaskViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = _binding.root

        context?.let {
            initViewModel(it)
        }
        observeData()
        return view
    }

    override fun onResume() {
        super.onResume()
        callData()
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        //observe user data
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            _binding.greeting.text = "Nice to meet you, ${user?.name}!"
        }

        //observe task count this month
        taskViewModel.pendingCountTasks.observe(viewLifecycleOwner) {countPending->
            _binding.pendingCount.text = countPending.toString()
        }
        taskViewModel.inProgressCountTasks.observe(viewLifecycleOwner) { countInProgress ->
            _binding.progressCount.text = countInProgress.toString()
        }
        taskViewModel.completedCountTasks.observe(viewLifecycleOwner) { countCompleted ->
            _binding.completeCount.text = countCompleted.toString()
        }

        //observe 3 latest task by status
        taskViewModel.pendingTasks.observe(viewLifecycleOwner) { pendingTasks ->
            taskViewModel.inProgressTasks.observe(viewLifecycleOwner) { inProgressTasks ->
                taskViewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
                    val categories = listOf("Pending", "On Progress", "Completed")
                    val tasks = mapOf(
                        "Pending" to pendingTasks,
                        "On Progress" to inProgressTasks,
                        "Completed" to completedTasks
                    )
                    context?.let {
                        val parentAdapter = HomeParentAdapter(it,categories, tasks)
                        _binding.rvParent.adapter = parentAdapter
                    }
                }
            }
        }

    }

    private fun callData() {
        userViewModel.getUserData()
        taskViewModel.getTaskCountByDateAndStatus(listOf("Pending", "Completed", "On Progress"))
        taskViewModel.getLimitTasksByStatus()
    }

    private fun initViewModel(it: Context) {
        val userRepository = UserRepository(
            userDao = AppDatabase.getInstance(it).userDao(),
            auth = Firebase.auth
        )
        val taskRepository = TaskRepository(
            taskDao = AppDatabase.getInstance(it).taskDao(),
            firestore = Firebase.firestore
        )

        val userFactory = UserViewModelFactory(userRepository)
        val taskFactory = TaskViewModelFactory(taskRepository)

        // Initialize the ViewModel
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]
        taskViewModel = ViewModelProvider(this, taskFactory)[TaskViewModel::class.java]
    }

}