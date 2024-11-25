package com.deo.todo_app.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.deo.todo_app.R
import com.deo.todo_app.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomCalendarAdapter(private val context: Context, private val days: List<Calendar>, private var tasksByDate: Map<String, List<Task>>, private val onDateSelected: (List<Task>,String) -> Unit) :
    BaseAdapter() {

    private var selectedDate: Calendar = Calendar.getInstance()


    override fun getCount(): Int {
        return days.size
    }

    override fun getItem(position: Int): Any {
        return days[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(R.layout.calendar_day, parent, false)

        //init widget
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val eventText = view.findViewById<TextView>(R.id.eventText1)
        val eventText2 = view.findViewById<TextView>(R.id.eventText2)
        val eventTextMore = view.findViewById<TextView>(R.id.eventTextMore)

        val calendar = days[position]
        val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        //set day
        dateText.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

        //get event
        val tasks = tasksByDate[formattedDate] ?: emptyList()

        //set max event is 2
        if (tasks.isEmpty()) {
            eventText.visibility = View.INVISIBLE
            eventText2.visibility = View.GONE
            eventTextMore.visibility = View.GONE
        }else{
            when(tasks.size){
                1 -> {
                    eventText.visibility = View.VISIBLE
                    eventText2.visibility = View.GONE
                    eventTextMore.visibility = View.GONE
                    eventText.text = tasks[0].title
                }
                2 -> {
                    eventText.visibility = View.VISIBLE
                    eventText2.visibility = View.VISIBLE
                    eventTextMore.visibility = View.GONE
                    eventText.text = tasks[0].title
                    eventText2.text = tasks[1].title
                }
                else -> {
                    eventText.visibility = View.VISIBLE
                    eventText2.visibility = View.VISIBLE
                    eventTextMore.visibility = View.VISIBLE
                    eventText.text = tasks[0].title
                    eventText2.text = tasks[1].title
                    eventTextMore.text = "+${tasks.size - 2} more.."
                }
            }
        }
        if (calendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
            calendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
        ) {
            dateText.setBackgroundResource(R.drawable.selected_date_background)
        } else {
            dateText.setBackgroundResource(android.R.color.transparent)
        }
        //setOnClickListener
        view.setOnClickListener {
            selectedDate = calendar
            notifyDataSetChanged()
            val tasksOnDate = tasks.filter { task ->
                val taskDate = Calendar.getInstance().apply { timeInMillis = task.date }
                taskDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        taskDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        taskDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
            }.sortedByDescending { it.date }
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
            onDateSelected(tasksOnDate, date)
        }

        return view
    }
    fun updateData(newTasks: Map<String, List<Task>>) {
        tasksByDate = newTasks
        notifyDataSetChanged()
    }

    fun updateSelectedDate(newSelectedDate: Calendar) {
        selectedDate = newSelectedDate
        notifyDataSetChanged()
    }
}