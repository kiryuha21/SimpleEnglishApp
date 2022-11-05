package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.databinding.FragmentReadingBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

        fillCard()

        fragBinding.readingBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        fragBinding.readingDoneButton.setOnClickListener {
            taskModel.user.value!!.XP += taskModel.currentTask.value!!.pointsXP
            taskModel.user.value!!.completedTasks += taskModel.currentTask.value!!.id
            taskModel.user.value!!.password = ""
            fragBinding.taskLoadingProgress.visibility = View.VISIBLE
            val jsonUser = Json.encodeToString(taskModel.user.value!!)
            val postBody = mapOf("id" to taskModel.user.value!!.id.toString(), "stringUser" to jsonUser)
            lifecycleScope.launch(Dispatchers.IO) {
                HttpsRequests().sendAsyncRequest("/update_user", postBody, HttpMethods.PUT)
            }.invokeOnCompletion {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, ChooseTask())
                    .commit()
            }
        }

        return fragBinding.root
    }

    private fun fillCard() = with(fragBinding.readingInclude) {
        readingHeaderImage.setImageResource(when(taskModel.tasksType.value) {
            Constants.audio -> R.drawable.music_disk
            Constants.theory -> R.drawable.study_hat
            Constants.insertWords -> R.drawable.task_list
            else -> R.drawable.book
        })

        readingHeaderPoints.text = "${taskModel.currentTask.value!!.pointsXP} XP"
        readingHeaderDescription.text = taskModel.currentTask.value!!.description
        readingHeaderCard.transitionName = taskModel.transitionName.value

        fragBinding.textContent.text = taskModel.currentTask.value!!.content.taskText
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