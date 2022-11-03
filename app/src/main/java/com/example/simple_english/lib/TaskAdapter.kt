package com.example.simple_english.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_english.R
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.databinding.TaskItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence

class TaskAdapter: RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    private val tasks = ArrayList<TaskHeader>()

    class TaskHolder(item: View): RecyclerView.ViewHolder(item) {
        private val binding = TaskItemBinding.bind(item)

        fun bind(task: TaskHeader) = with(binding) {
            studyType.setImageResource(when(task.taskType) {
                Constants.audio -> 1
                Constants.theory -> 2
                Constants.insertWords -> 3
                else -> R.drawable.book
            })
            taskNameTV.text = task.description
            pointsTV.text = task.pointsXP.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskHolder(view)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    suspend fun loadFromDB(parent: ViewGroup, type: String) {
        val requests = HttpsRequests()
        val jsonTasks = requests.sendAsyncRequest("/find_task_headers_by_type", mapOf("type" to type), HttpMethods.POST)
        tasks.addAll(Json.decodeFromString<ArrayList<TaskHeader>>(jsonTasks))
    }
}