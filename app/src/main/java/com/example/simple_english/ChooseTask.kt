package com.example.simple_english

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.databinding.FragmentChooseTaskBinding
import com.example.simple_english.lib.TaskAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ChooseTask : Fragment() {
    private lateinit var fragBinding: FragmentChooseTaskBinding
    private val adapter = TaskAdapter()

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
        adapter.tasks.addAll(Json.decodeFromString<ArrayList<TaskHeader>>(arguments?.getString("tasks")!!))
        fragBinding.optionsRecycle.adapter = adapter
        fragBinding.optionsRecycle.layoutManager = LinearLayoutManager(context)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChooseTask()
    }
}