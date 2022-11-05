package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.FragmentChooseTaskBinding
import com.example.simple_english.lib.TaskAdapter
import com.example.simple_english.lib.TaskModel

class ChooseTask : Fragment() {
    private lateinit var fragBinding: FragmentChooseTaskBinding
    private val taskModel: TaskModel by activityViewModels()
    private val adapter = TaskAdapter {
        val recycle = fragBinding.optionsRecycle
        taskModel.currentTask.value = taskModel.tasks.value!![recycle.getChildAdapterPosition(it)]
        taskModel.transitionName.value = it.transitionName
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(
                R.id.fragmentContainer, when (taskModel.tasksType.value!!) {
                    Constants.audio -> Audio()
                    Constants.theory -> Theory()
                    Constants.insertWords -> InsertWords()
                    else -> Reading()
                }
            )
            .addSharedElement(it, it.transitionName)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragments_transition)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragments_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentChooseTaskBinding.inflate(inflater)
        return fragBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

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