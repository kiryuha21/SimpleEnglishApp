package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.FragmentReadingBinding
import com.example.simple_english.lib.TaskModel

class Reading : Fragment() {
    private lateinit var fragBinding: FragmentReadingBinding
    private val taskModel: TaskModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragments_transition)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragments_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentReadingBinding.inflate(inflater)

        fragBinding.readingHeaderImage.setImageResource(when(taskModel.tasksType.value) {
            Constants.audio -> R.drawable.music_disk
            Constants.theory -> R.drawable.study_hat
            Constants.insertWords -> R.drawable.task_list
            else -> R.drawable.book
        })

        fragBinding.readingHeaderPoints.text = "${taskModel.currentTask.value!!.pointsXP} XP"
        fragBinding.readingHeaderDescription.text = taskModel.currentTask.value!!.description
        fragBinding.textContent.text = taskModel.currentTask.value!!.content.taskText
        fragBinding.readingHeaderCard.transitionName = taskModel.transitionName.value

        return fragBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = Reading()
    }
}