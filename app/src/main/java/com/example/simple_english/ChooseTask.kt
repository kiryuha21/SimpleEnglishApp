package com.example.simple_english

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simple_english.databinding.FragmentChooseTaskBinding
import com.example.simple_english.lib.TaskAdapter
import com.example.simple_english.lib.TaskModel

class ChooseTask : Fragment() {
    private lateinit var fragBinding: FragmentChooseTaskBinding
    private val adapter = TaskAdapter()
    private val taskModel: TaskModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentChooseTaskBinding.inflate(inflater)
        return fragBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskModel.tasks.observe(activity as LifecycleOwner) {
            adapter.tasks = it
        }
        fragBinding.optionsRecycle.adapter = adapter
        fragBinding.optionsRecycle.layoutManager = LinearLayoutManager(context)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChooseTask()
    }
}