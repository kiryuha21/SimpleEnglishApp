package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.databinding.FragmentMemorisingBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class Memorising : Fragment() {
    private lateinit var fragBinding: FragmentMemorisingBinding
    private val requests = HttpsRequests()
    private val taskModel: TaskModel by activityViewModels()
    private val searchBase = "https://api.openverse.engineering/v1/images?q="
    private var correctAnswer = false

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
        fragBinding = FragmentMemorisingBinding.inflate(inflater)

        fillCard()
        fillContent()
        setButtonClicks()

        return fragBinding.root
    }

    private fun setButtonClicks() {
        fragBinding.memoBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        fragBinding.memoReadyButton.setOnClickListener {
            it.isEnabled = false
            fragBinding.memoBackButton.isEnabled = false
            fragBinding.memoDoneButton.isEnabled = true

            correctAnswer = taskModel.currentTask.value!!.content.taskText!!.lowercase() == fragBinding.memoWordEdit.text.toString().lowercase()
            val text = if (correctAnswer) getText(R.string.correct_answer) else getText(R.string.incorrect_answer)

            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }

        fragBinding.memoDoneButton.setOnClickListener {
            taskModel.currentTask.value!!.content.nextNoticeIn = setNextNotice(taskModel.currentTask.value!!.content.nextNoticeIn!!)
            val jsonContent = Json.encodeToString(taskModel.currentTask.value!!.content)
            val body = mapOf("id" to taskModel.currentTask.value!!.content.id.toString(), "stringTask" to jsonContent)
            setLoadState(isActive = true, withLoad = true)
            lifecycleScope.launch(Dispatchers.IO) {
                requests.sendAsyncRequest("update_task_content_by_id", body, HttpMethods.POST)
            }.invokeOnCompletion {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, ChooseTask())
                    .commit()
            }
        }
    }

    private fun setNextNotice(currentNotice: String): String = when(currentNotice) {
        "'0 seconds'" -> "'30 minutes'"
        "'30 minutes'" -> "'1 day'"
        "'1 day'" -> "'7 days'"
        "'7 days'" -> "'1 month'"
        "'1 month'" -> "'6 months'"
        else -> Constants.memoFinished
    }

    private fun fillCard() = with(fragBinding.memorisingInclude) {
        readingHeaderImage.setImageResource(when(taskModel.currentTask.value!!.taskType) {
            Constants.audio -> R.drawable.music_disk
            Constants.theory -> R.drawable.study_hat
            Constants.insertWords -> R.drawable.task_list
            Constants.memorising -> R.drawable.ic_calendar
            else -> R.drawable.book
        })

        readingHeaderPoints.text = "${taskModel.currentTask.value!!.pointsXP} XP"
        readingHeaderDescription.text = taskModel.currentTask.value!!.description
        readingHeaderCard.transitionName = taskModel.transitionName.value

        //fragBinding.textContent.text = taskModel.currentTask.value!!.content.taskText
    }

    private fun fillContent() {
        val headers = mapOf("Authorization" to "Bearer vZufVHly8ZufjPl0LKbX2Og8KbBauG")
        val url = searchBase + taskModel.currentTask.value!!.content.taskText
        setLoadState(isActive = true, withLoad = true)

        lifecycleScope.launch(Dispatchers.IO) {
            val jsonResp = requests.getWithHeaders(url, headers)
            val picUrl = jsonResp.jsonObject["results"]?.jsonArray?.get(0)?.jsonObject?.get("url").toString()
            val picUri = picUrl.toUri().buildUpon().scheme("https").build()

            fragBinding.wordPicture.load(picUri) {
                placeholder(R.drawable.loading_animation)
                error(R.drawable.ic_broken_image)
            }
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                setLoadState(isActive = false, withLoad = true)
            }
        }
    }

    private fun setLoadState(isActive: Boolean, withLoad: Boolean) = with(fragBinding) {
        memoLoading.visibility = if (isActive && withLoad) {
            View.VISIBLE
        } else {
            View.GONE
        }

        memoReadyButton.isEnabled = !isActive
        memoBackButton.isEnabled = !(withLoad && isActive)
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
        fun newInstance() = Memorising()
    }
}