package com.example.simple_english

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.FragmentMemorisingBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class Memorising : Fragment() {
    private lateinit var fragBinding: FragmentMemorisingBinding
    private val requests = HttpsRequests()
    private val taskModel: TaskModel by activityViewModels()
    private val searchBase = "https://api.openverse.engineering/v1/images?q="

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

        return fragBinding.root
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