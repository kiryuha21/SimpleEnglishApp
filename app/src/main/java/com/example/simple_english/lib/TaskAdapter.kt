package com.example.simple_english.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_english.R.layout
import com.example.simple_english.R.drawable
import com.example.simple_english.data.Constants
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.databinding.TaskItemBinding

class TaskAdapter(private val clickListener: (View) -> Unit): RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    var tasks = ArrayList<TaskHeader>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout.task_item, parent, false)
        return TaskHolder(view)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.bind(tasks[position], position, clickListener)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    class TaskHolder(item: View): RecyclerView.ViewHolder(item) {
        private val binding = TaskItemBinding.bind(item)

        fun bind(task: TaskHeader, position: Int, clickListener: (View) -> Unit) = with(binding) {
            studyType.setImageResource(when(task.taskType) {
                Constants.audio -> drawable.music_disk
                Constants.theory -> drawable.study_hat
                Constants.insertWords -> drawable.task_list
                else -> drawable.book
            })
            taskNameTV.text = task.description
            pointsTV.text = "${task.pointsXP} XP"
            taskCard.setOnClickListener(clickListener)
            taskCard.transitionName = String.format(Constants.taskHeaderTransitionName, position)
        }
    }
}