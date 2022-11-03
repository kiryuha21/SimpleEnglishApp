package com.example.simple_english

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.FragmentChooseTaskBinding
import com.example.simple_english.lib.TaskAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        fragBinding.optionsRecycle.layoutManager = LinearLayoutManager(this.context)
        if (container != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                adapter.loadFromDB(container, Constants.reading)
            }.invokeOnCompletion {
                fragBinding.optionsRecycle.adapter = adapter
                fragBinding.optionsRecycle.adapter?.notifyDataSetChanged()
            }
        }

        return fragBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChooseTask()
    }
}