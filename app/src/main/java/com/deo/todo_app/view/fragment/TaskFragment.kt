package com.deo.todo_app.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.databinding.FragmentTaskBinding
import com.deo.todo_app.view.adapter.CustomCalendarAdapter
import com.deo.todo_app.view.adapter.TaskByDateAdapter
import com.deo.todo_app.view.bottomSheet.UpsertTaskBottomSheet
import com.deo.todo_app.viewModel.CalendarViewModel
import com.deo.todo_app.viewModel.factory.CalendarViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskFragment : Fragment() {
    private lateinit var _binding: FragmentTaskBinding
    private lateinit var calendarAdapter: CustomCalendarAdapter
    private lateinit var calendarViewModel: CalendarViewModel
    private val days: MutableList<Calendar> = mutableListOf()

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedDate: Calendar = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        val view = _binding.root

        Log.e("monthYear", "$currentMonth $currentYear ")
        context?.let {
            initViewModel(it)
            calendarViewModel.tasksWithDate.observe(viewLifecycleOwner){ tasks->
                if (!::calendarAdapter.isInitialized) {
                    calendarAdapter = CustomCalendarAdapter(it, days, tasks) { tasksOnDate,date ->
                        //display task by date
                        _binding.taskOnDate.text = "Task on $date"
                        val adapter = TaskByDateAdapter(parentFragmentManager, tasksOnDate)
                        _binding.rvTaskOnDate.adapter = adapter
                        if (tasksOnDate.isEmpty()){
                            _binding.emptyTask.visibility = View.VISIBLE
                            _binding.rvTaskOnDate.visibility = View.GONE
                        }else{
                            _binding.emptyTask.visibility = View.GONE
                            _binding.rvTaskOnDate.visibility = View.VISIBLE
                        }
                    }
                    calendarAdapter.updateSelectedDate(selectedDate)
                    _binding.calendarGridView.adapter = calendarAdapter
                }else{
                    calendarAdapter.updateData(tasks)
                }
            }
            loadCalendarData(currentMonth, currentYear)
            _binding.apply {
                addTask.setOnClickListener{
                    val bottomSheet = UpsertTaskBottomSheet(requireContext(),null){
                        calendarViewModel.loadTask(getMonthStartMillis(currentYear, currentMonth), getMonthStartMillis(currentYear, currentMonth + 1))
                    }
                    bottomSheet.show(parentFragmentManager, "UpsertTaskBottomSheet")
                }

                nextButton.setOnClickListener {
                    currentMonth += 1
                    if (currentMonth > 11) {
                        currentMonth = 0
                        currentYear += 1
                    }
                    loadCalendarData(currentMonth, currentYear)
                }

                prevButton.setOnClickListener {
                    currentMonth -= 1
                    if (currentMonth < 0) {
                        currentMonth = 11
                        currentYear -= 1
                    }
                    loadCalendarData(currentMonth, currentYear)
                }

            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        calendarViewModel.loadTask(getMonthStartMillis(currentYear, currentMonth), getMonthStartMillis(currentYear, currentMonth + 1))
    }

    private fun initViewModel(it: Context) {
        val taskRepository = TaskRepository(
            taskDao = AppDatabase.getInstance(it).taskDao(),
            firestore = Firebase.firestore
        )
        val calendarFactory = CalendarViewModelFactory(taskRepository)
        calendarViewModel = ViewModelProvider(this,calendarFactory)[CalendarViewModel::class.java]
    }

    private fun loadCalendarData(month: Int, year: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        days.clear()

        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth + 1)
        for (i in 1 until firstDayOfMonth) {
            days.add(tempCalendar.clone() as Calendar)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        for (i in 1..maxDaysInMonth) {
            val day = calendar.clone() as Calendar
            day.set(Calendar.DAY_OF_MONTH, i)
            days.add(day)
        }

        val remainingDays = 7 - (days.size % 7)
        if (remainingDays < 7) {
            tempCalendar.time = calendar.time
            tempCalendar.add(Calendar.MONTH, 1)
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

            for (i in 1..remainingDays) {
                days.add(tempCalendar.clone() as Calendar)
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Get month and year
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYearString = monthYearFormat.format(calendar.time)
        _binding.monthText.text = monthYearString
        if (selectedDate.get(Calendar.MONTH) != month || selectedDate.get(Calendar.YEAR) != year) {
            selectedDate.set(year, month, 1)
        }
        if (::calendarAdapter.isInitialized) {
            calendarAdapter.updateSelectedDate(selectedDate)
        }
    }
    private fun getMonthStartMillis(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}