package com.example.simple_english

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.databinding.FragmentAudioBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

class Audio : Fragment() {
    private lateinit var fragBinding: FragmentAudioBinding
    private val requests = HttpsRequests()
    private val taskModel: TaskModel by activityViewModels()
    private val player = MediaPlayer()
    private lateinit var currentActivity: FragmentActivity
    private var choiceCounter = 0
    private var tasksCount = 0
    private var correctAnswers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentActivity = requireActivity()

        sharedElementEnterTransition = TransitionInflater.from(currentActivity)
            .inflateTransition(R.transition.fragments_transition)
        sharedElementReturnTransition = TransitionInflater.from(currentActivity)
            .inflateTransition(R.transition.fragments_transition)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentAudioBinding.inflate(inflater)

        player.setAudioAttributes(AudioAttributes
            .Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        var finalURL = ""

        if (!requests.isNetworkAvailable(currentActivity)) {
            Toast.makeText(currentActivity, getText(R.string.connect_and_reload), Toast.LENGTH_SHORT).show()
            setLoadState(isActive = true, withLoad = false)
        } else {
            setLoadState(isActive = true, withLoad = true)
            lifecycleScope.launch(Dispatchers.IO) {
                finalURL =
                    requests.getMusicFileUrl(taskModel.currentTask.value!!.content.musicURL!!)
            }.invokeOnCompletion {
                if (finalURL.isNotEmpty()) {
                    player.setDataSource(finalURL)
                    player.prepareAsync()
                    currentActivity.runOnUiThread {
                        setLoadState(isActive = false, withLoad = true)
                    }
                } else {
                    currentActivity.runOnUiThread {
                        Toast.makeText(currentActivity, "С аудиофайлом возникла проблема", Toast.LENGTH_SHORT).show()
                        setLoadState(isActive = true, withLoad = false)
                    }
                }
            }
        }

        fillCard()
        fillRadioButtons()
        setButtonListeners()

        fragBinding.audioCurrentTask.text = taskModel.currentTask.value!!.content.questions!![choiceCounter]
        tasksCount = taskModel.currentTask.value!!.content.correctVariants!!.size

        return fragBinding.root
    }

    private fun setLoadState(isActive: Boolean, withLoad: Boolean) = with(fragBinding) {
        audioLoadingProgress.visibility = if (isActive && withLoad) {
            View.VISIBLE
        } else {
            View.GONE
        }

        audioStartButton.isEnabled = !isActive
        audioStopButton.isEnabled = !isActive
        audioRewindButton.isEnabled = !isActive
        audioReadyButton.isEnabled = !isActive

        audioBackButton.isEnabled = !(withLoad && isActive)
    }

    private fun setButtonListeners() {
        fragBinding.audioStartButton.setOnClickListener {
            if (!player.isPlaying) {
                player.start()
            }
        }

        fragBinding.audioStopButton.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            }
        }

        fragBinding.audioRewindButton.setOnClickListener {
            player.seekTo(0)
        }

        fragBinding.audioReadyButton.setOnClickListener {
            onReadyButtonClick()
        }

        fragBinding.audioBackButton.setOnClickListener {
            currentActivity.supportFragmentManager.popBackStack()
        }

        fragBinding.audioDoneButton.setOnClickListener {
            player.release()
            taskModel.user.value!!.XP += (taskModel.currentTask.value!!.pointsXP * (correctAnswers.toFloat() / tasksCount)).toInt()
            taskModel.user.value!!.completedTasks += taskModel.currentTask.value!!.id!!
            taskModel.user.value!!.password = ""
            fragBinding.audioLoadingProgress.visibility = View.VISIBLE
            val jsonUser = Json.encodeToString(taskModel.user.value!!)
            val postBody = mapOf("id" to taskModel.user.value!!.id.toString(), "stringUser" to jsonUser)
            lifecycleScope.launch(Dispatchers.IO) {
                HttpsRequests().sendAsyncRequest("/update_user", postBody, HttpMethods.PUT)
            }.invokeOnCompletion {
                currentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, ChooseTask())
                    .commit()
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

    private fun onReadyButtonClick() {
        val rightOption = taskModel.currentTask.value!!.content.correctVariants!![choiceCounter]
        val chosenText = fragBinding.root.findViewById<RadioButton>(fragBinding.groupRB.checkedRadioButtonId).text

        ++choiceCounter
        if (choiceCounter == tasksCount) {
            fragBinding.audioDoneButton.isEnabled = true
            fragBinding.audioReadyButton.isEnabled = false
            fragBinding.groupRB.isEnabled = false
        } else {
            fillRadioButtons()
            fragBinding.audioCurrentTask.text = taskModel.currentTask.value!!.content.questions!![choiceCounter]
        }

        val toastText = if (rightOption == chosenText) "Правильный ответ" else "неправильный ответ"
        Toast.makeText(currentActivity, toastText, Toast.LENGTH_SHORT).show()
    }

    private fun fillRadioButtons() = with(fragBinding) {
        val currentVariants = taskModel.currentTask.value!!.content.taskVariants?.get(choiceCounter)
        if (currentVariants != null) {
            firstRB.text = currentVariants[0]
            secondRB.text = currentVariants[1]
            thirdRB.text = currentVariants[2]
        }
    }

    private fun fillCard() = with(fragBinding.audioInclude) {
        readingHeaderImage.setImageResource(when(taskModel.currentTask.value!!.taskType) {
            Constants.audio -> R.drawable.music_disk
            Constants.theory -> R.drawable.study_hat
            Constants.insertWords -> R.drawable.task_list
            else -> R.drawable.book
        })

        readingHeaderPoints.text = "${taskModel.currentTask.value!!.pointsXP} XP"
        readingHeaderDescription.text = taskModel.currentTask.value!!.description
        readingHeaderCard.transitionName = taskModel.transitionName.value
    }

    companion object {
        @JvmStatic
        fun newInstance() = Audio()
    }
}