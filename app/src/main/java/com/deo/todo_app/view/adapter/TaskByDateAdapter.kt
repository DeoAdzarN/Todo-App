package com.deo.todo_app.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.deo.todo_app.R
import com.deo.todo_app.helper.DateTimeHelper
import com.deo.todo_app.model.Task
import com.deo.todo_app.view.bottomSheet.UpsertTaskBottomSheet

class TaskByDateAdapter (private val fragmentManager: FragmentManager, private var tasks: List<Task>) : RecyclerView.Adapter<TaskByDateAdapter.ViewHolder>()  {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val desc: TextView = itemView.findViewById(R.id.desc)
        val status: TextView = itemView.findViewById(R.id.status)
        val time: TextView = itemView.findViewById(R.id.time)

    }
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_task_calendar, parent, false))
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.desc.text = task.description
        holder.status.text = task.status
        when(task.status){
            "Pending" -> holder.status.setBackgroundResource(R.drawable.background_status_task_pending)
            "On Progress" -> holder.status.setBackgroundResource(R.drawable.background_status_task_progress)
            "Completed" -> holder.status.setBackgroundResource(R.drawable.background_status_task_complete)
        }
        holder.time.text = DateTimeHelper().convertToTimeOnly(task.date)
        holder.itemView.setOnClickListener {
            val bottomSheetFragment = UpsertTaskBottomSheet(holder.itemView.context,task){}
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }
    }
}