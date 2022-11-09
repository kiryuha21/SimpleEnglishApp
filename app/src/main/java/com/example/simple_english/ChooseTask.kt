package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
                R.id.fragmentContainer, when (taskModel.currentTask.value!!.taskType) {
                    Constants.audio -> Audio()
                    Constants.theory -> Theory()
                    Constants.insertWords -> InsertWords()
                    Constants.memorising -> Memorising()
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
            for (i in it.indices) {
                for (j in taskModel.user.value!!.completedTasks) {
                    if (it[i].id == j) {
                        it[i].pointsXP = Constants.doneTask
                    }
                }
            }
            adapter.tasks = it
        }
        fragBinding.optionsRecycle.adapter = adapter
        fragBinding.optionsRecycle.layoutManager = LinearLayoutManager(context)

        if (taskModel.tasks.value!!.first().taskType == Constants.memorising) {
            val button = Button(context)
            button.text = getText(R.string.add_word)
            fragBinding.chooseTaskLinearLayout.addView(button)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChooseTask()
    }
}