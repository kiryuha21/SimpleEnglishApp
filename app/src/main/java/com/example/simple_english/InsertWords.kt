package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.text.HtmlCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.databinding.FragmentInsertWordsBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InsertWords : Fragment() {
    private lateinit var fragBinding: FragmentInsertWordsBinding
    private val taskModel: TaskModel by activityViewModels()
    private var choiceCounter = 0
    private var tasksCount = 0
    private var correctAnswers = 0

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
        fragBinding = FragmentInsertWordsBinding.inflate(inflater)

        tasksCount = taskModel.currentTask.value!!.content.correctVariants.size

        fillCard()
        fillRadioButtons()
        fragBinding.insertWordsInsertButton.setOnClickListener {
            onInsertButtonClick()
        }

        fragBinding.insertWordsBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        fragBinding.insertWordsDoneButton.setOnClickListener {
            taskModel.user.value!!.XP += (taskModel.currentTask.value!!.pointsXP * (correctAnswers.toFloat() / tasksCount)).toInt()
            taskModel.user.value!!.completedTasks += taskModel.currentTask.value!!.id
            taskModel.user.value!!.password = ""
            fragBinding.insertWordsLoadingProgress.visibility = View.VISIBLE
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

    private fun fillCard() = with(fragBinding.insertWordsInclude) {
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

    private fun onInsertButtonClick() {
        val rightOption = taskModel.currentTask.value!!.content.correctVariants[choiceCounter]
        val chosenText = fragBinding.root.findViewById<RadioButton>(fragBinding.groupRB.checkedRadioButtonId).text

        ++choiceCounter
        if (choiceCounter == tasksCount) {
            fragBinding.insertWordsDoneButton.isEnabled = true
            fragBinding.insertWordsInsertButton.isEnabled = false
            fragBinding.groupRB.isEnabled = false
        } else {
            fillRadioButtons()
        }

        fragBinding.groupRB.clearCheck()
        val text = fragBinding.textContent.text
        val htmlParseText = if (rightOption == chosenText) {
            ++correctAnswers
            text.replaceFirst(Regex("_"), String.format(Constants.correctAnswer, rightOption))
        } else {
            text.replaceFirst(Regex("_"), String.format(Constants.wrongAnswer, chosenText))
        }
        fragBinding.textContent.text = HtmlCompat.fromHtml(htmlParseText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private fun fillRadioButtons() = with(fragBinding) {
        val currentVariants = taskModel.currentTask.value!!.content.taskVariants[choiceCounter]
        firstRB.text = currentVariants[0]
        secondRB.text = currentVariants[1]
        thirdRB.text = currentVariants[2]
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
        fun newInstance() = InsertWords()
    }
}