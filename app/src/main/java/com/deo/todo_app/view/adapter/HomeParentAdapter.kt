package com.deo.todo_app.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deo.todo_app.R
import com.deo.todo_app.model.Task
import com.deo.todo_app.utils.TaskStatus
import com.deo.todo_app.view.activity.MainActivity
import com.deo.todo_app.view.activity.ViewAllTaskActivity

class HomeParentAdapter(private val context: Context, private val status: List<String>, private val tasks: Map<String, List<Task>>) :
    RecyclerView.Adapter<HomeParentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val viewAll: TextView = itemView.findViewById(R.id.viewAll)
        val emptyTask: TextView = itemView.findViewById(R.id.emptyTask)
        val rvChild: RecyclerView = itemView.findViewById(R.id.rvChild)
        val parent: CardView = itemView.findViewById(R.id.parent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_home_parent, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return status.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val status = status[position]
        holder.title.text = status

        if (tasks[status]?.isEmpty() == true) {
            holder.emptyTask.visibility = View.VISIBLE
            holder.rvChild.visibility = View.GONE
        } else {
            holder.emptyTask.visibility = View.GONE
            holder.rvChild.visibility = View.VISIBLE
        }

        val childAdapter = HomeChildAdapter( (holder.itemView.context as MainActivity).supportFragmentManager,tasks[status] ?: emptyList())
        holder.rvChild.adapter = childAdapter
        holder.rvChild.layoutManager = LinearLayoutManager(holder.itemView.context)

        holder.viewAll.setOnClickListener {
            context.startActivity(Intent(context, ViewAllTaskActivity::class.java).apply {
                putExtra("status", status)
            })
        }

        when (status) {
            "Pending" -> {
                holder.parent.setCardBackgroundColor(context.getColor(R.color.yellow))
                holder.emptyTask.text = "You don't have any pending task"
            }
            "On Progress" -> {
                holder.parent.setCardBackgroundColor(context.getColor(R.color.light_blue))
                holder.emptyTask.text = "You don't have any progress task"
            }
            "Completed" -> {
                holder.parent.setCardBackgroundColor(context.getColor(R.color.green))
                holder.emptyTask.text = "You don't have any completed task"
            }
        }
    }

}