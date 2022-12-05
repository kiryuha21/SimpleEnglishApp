package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.databinding.FragmentReadingBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import com.example.simple_english.lib.getPostBodyForUserXpUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Reading : Fragment() {
    private lateinit var fragBinding: FragmentReadingBinding
    private val requests = HttpsRequests()
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
            onReadingDoneButtonClick()
        }

        return fragBinding.root
    }

    private fun onReadingDoneButtonClick() {
        if (!requests.isNetworkAvailable(requireActivity())) {
            Toast.makeText(requireActivity(), getText(R.string.no_connection), Toast.LENGTH_SHORT).show()
            return
        }

        fragBinding.taskLoadingProgress.visibility = View.VISIBLE
        val postBody = getPostBodyForUserXpUpdate(taskModel.user.value!!, taskModel.currentTask.value!!.pointsXP, taskModel.currentTask.value!!.id!!)
        lifecycleScope.launch(Dispatchers.IO) {
            requests.sendAsyncRequest("/update_user", postBody, HttpMethods.PUT)
        }.invokeOnCompletion {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, ChooseTask())
                .commit()
        }
    }

    private fun fillCard() = with(fragBinding.readingInclude) {
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
        fun newInstance() = Reading()
    }
}