package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.FragmentTheoryBinding
import com.example.simple_english.lib.TaskModel

class Theory : Fragment() {
    private lateinit var fragBinding: FragmentTheoryBinding
    private val taskModel: TaskModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragments_transition)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.fragments_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentTheoryBinding.inflate(inflater)

        fillCard()

        fragBinding.theoryDoneButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return fragBinding.root
    }

    private fun fillCard() = with(fragBinding.theoryInclude) {
        readingHeaderImage.setImageResource(when(taskModel.currentTask.value!!.taskType) {
            Constants.audio -> R.drawable.music_disk
            Constants.theory -> R.drawable.study_hat
            Constants.insertWords -> R.drawable.task_list
            else -> R.drawable.book
        })

        readingHeaderPoints.text = "${taskModel.currentTask.value!!.pointsXP} XP"
        readingHeaderDescription.text = taskModel.currentTask.value!!.description
        readingHeaderCard.transitionName = taskModel.transitionName.value

        fragBinding.textContent.text = HtmlCompat.fromHtml(taskModel.currentTask.value!!.content.taskText!!, FROM_HTML_MODE_COMPACT)
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
        fun newInstance() = Theory()
    }
}