package com.example.simple_english

import android.app.AlertDialog
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.TaskContent
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.databinding.FragmentChooseTaskBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskAdapter
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.bush.translator.Language
import java.sql.Timestamp

class ChooseTask : Fragment() {
    private lateinit var fragBinding: FragmentChooseTaskBinding
    private val taskModel: TaskModel by activityViewModels()
    private val requests = HttpsRequests()
    private val adapter = TaskAdapter {  // onClick function for every adapter element
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

        buttonConfigure()
    }

    // button to add word in memorising activity
    private fun buttonConfigure() {
        if (taskModel.currentType.value!! == Constants.memorising) {
            val edit = EditText(context)

            val addWordAlert = AlertDialog.Builder(context)
                .setTitle("Добавьте слово")
                .setView(edit)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                    run {
                        alertConfirmPressed(edit.text.toString())
                        dialogInterface.dismiss()
                    }
                }
                .create()

            val button = Button(context)
            button.setOnClickListener {
                addWordAlert.show()
            }

            button.text = getText(R.string.add_word)
            fragBinding.chooseTaskLinearLayout.addView(button)
        }
    }

    // handling of adding a new memorising word
    private fun alertConfirmPressed(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(context, "Слово не может быть пустым!", Toast.LENGTH_SHORT).show()
            return
        }

        fragBinding.memoProgress.visibility = View.VISIBLE

        lateinit var response: String
        lateinit var newHeader: TaskHeader

        lifecycleScope.launch(Dispatchers.IO) {
            val header = formHeader(text)
            val jsonHeader = Json.encodeToString(header)
            response = requests.sendAsyncRequest("/add_task_header", mapOf("stringTaskHeader" to jsonHeader), HttpMethods.POST)
        }.invokeOnCompletion {
            newHeader = Json.decodeFromString(response)
            taskModel.user.value!!.startedMemories += newHeader.id!!
            taskModel.user.value!!.password = ""
            val jsonUser = Json.encodeToString(taskModel.user.value!!)
            lifecycleScope.launch(Dispatchers.IO) {
                requests.sendAsyncRequest("/update_user", mapOf("id" to taskModel.user.value!!.id.toString(), "stringUser" to jsonUser), HttpMethods.PUT)
            }.invokeOnCompletion {
                taskModel.tasks.value!!.add(newHeader)

                val recycle = fragBinding.optionsRecycle

                requireActivity().runOnUiThread {
                    recycle.adapter!!.notifyItemInserted(recycle.size)
                    recycle.scrollToPosition(recycle.adapter!!.itemCount - 1)
                    fragBinding.memoProgress.visibility = View.GONE
                }
            }
        }
    }

    // forms header to add new memorising task
    private suspend fun formHeader(description: String): TaskHeader {
        val translator = me.bush.translator.Translator()

        return TaskHeader(
            id = null,
            taskType = Constants.memorising,
            pointsXP = 10,
            description = description,
            content = TaskContent(
                id = null,
                taskText = translator.translate(
                    description,
                    Language.ENGLISH,
                    Language.RUSSIAN
                ).translatedText,
                taskVariants = null,
                correctVariants = null,
                questions = null,
                memLastUpdate = Timestamp(System.currentTimeMillis()),
                nextNoticeIn = "'0 seconds'",
                musicURL = null
            )
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChooseTask()
    }
}