package com.deo.todo_app.view.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.deo.todo_app.R
import com.deo.todo_app.model.Task
import com.deo.todo_app.view.activity.MainActivity
import com.deo.todo_app.view.bottomSheet.UpsertTaskBottomSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllTaskByStatusAdapter (private val fragmentManager:FragmentManager, private val tasks: List<Task>) : RecyclerView.Adapter<AllTaskByStatusAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val desc: TextView = itemView.findViewById(R.id.desc)
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val dateRemind: TextView = itemView.findViewById(R.id.dateRemind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_task_home, parent, false))
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        Log.e("status", "onBindViewHolder: ${task.status}", )
        holder.title.text = task.title
        holder.desc.text = task.description

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(task.date)
        val dateReminders = Date(task.reminder)

        val formattedDate = dateFormat.format(date)
        val formattedTime = timeFormat.format(date)
        val formattedDateRemind = dateFormat.format(dateReminders)
        val formattedTimeRemind = timeFormat.format(dateReminders)

        holder.date.text =formattedDate
        holder.time.text = formattedTime

        holder.dateRemind.text = "Remind on $formattedDateRemind · $formattedTimeRemind"

        holder.itemView.setOnClickListener {
            val bottomSheetFragment = UpsertTaskBottomSheet(holder.itemView.context,task){}
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }
    }
}