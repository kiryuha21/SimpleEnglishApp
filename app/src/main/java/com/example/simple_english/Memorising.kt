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
import coil.load
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.FragmentMemorisingBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import io.ktor.util.Identity.encode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class Memorising : Fragment() {
    private lateinit var fragBinding: FragmentMemorisingBinding
    private val requests = HttpsRequests()
    private val taskModel: TaskModel by activityViewModels()
    private val searchBase = "https://www.google.com/search?q=%s&tbm=isch"

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
        val url = String.format(searchBase, taskModel.currentTask.value!!.description)
        println(url)
        lateinit var image: Element
        lifecycleScope.launch(Dispatchers.IO) {
            val doc = Jsoup.connect(url).get()
            image = doc.select("img")[0]
        }.invokeOnCompletion {
            lifecycleScope.launch(Dispatchers.IO) {
                fragBinding.wordPicture.load(image.attr("data-src")) {
                    placeholder(R.drawable.loading_animation)
                }
            }
        }
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