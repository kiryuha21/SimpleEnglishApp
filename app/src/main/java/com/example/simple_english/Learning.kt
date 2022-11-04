package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityLearningBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Learning : AppCompatActivity() {
    private lateinit var binding: ActivityLearningBinding
    private lateinit var user: User
    private lateinit var learningType: String
    private val taskModel: TaskModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("user") as User
        learningType = intent.getStringExtra("learning_type")!!
        binding.learningTypeTV.text = learningType

        setNavigationActions()
        setNavHeaderText()

        setTasks(learningType)
    }

    private fun taskClickHandling(learningType: String) {
        println("so here")
        val fragment = supportFragmentManager.findFragmentByTag("chooseTask")!!
        println("here")
        val recycle = fragment.requireView().findViewById<RecyclerView>(R.id.optionsRecycle)
        val count = recycle.adapter!!.itemCount
        println(count)
        for (i in 0 until count) {
            val holder = recycle.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                val card = holder.itemView.findViewById<CardView>(R.id.taskCard)
                card.setOnClickListener {
                    card.transitionName = "cardHeading"

                    supportFragmentManager
                        .beginTransaction()
                        .addSharedElement(card, "cardHeading")
                        .replace(R.id.fragmentContainer, when(learningType) {
                            Constants.audio -> Audio()
                            Constants.theory -> Theory()
                            Constants.insertWords -> InsertWords()
                            else -> Reading()
                        })
                        .commit()
                }
            }
        }
    }

    private fun setTasks(learningType: String) {
        binding.fragmentLoadingProgress.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            taskModel.tasks.postValue(Json.decodeFromString<ArrayList<TaskHeader>>(loadFromDB(learningType)))
        }.invokeOnCompletion {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, ChooseTask(), "ChooseTask")
                .commit()

            taskClickHandling(learningType)

            runOnUiThread {
                binding.fragmentLoadingProgress.visibility = View.GONE
            }
        }
    }

    private suspend fun loadFromDB(type: String): String {
        return HttpsRequests().sendAsyncRequest("/find_task_headers_by_type", mapOf("type" to type), HttpMethods.POST)
    }

    private fun setNavHeaderText() {
        val navHeader = binding.navigation.commonNavigation.getHeaderView(0)
        val userGreetTV = navHeader.findViewById<TextView>(R.id.nav_header_greeting)
        userGreetTV.text = String.format(getText(R.string.nav_header_greeting).toString(), user.name ?: "Гость")
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> {
                    val educationIntent = Intent(this@Learning, MainMenu::class.java)
                    drawer.closeDrawer(GravityCompat.START)
                    educationIntent.putExtra("user", user)
                    startActivity(educationIntent, ActivityOptions.makeSceneTransitionAnimation(this@Learning).toBundle())
                    supportFinishAfterTransition()
                }
                else -> Toast.makeText(this@Learning, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }

        navigation.settingsButton.setOnClickListener {
            val settingsIntent = Intent(this@Learning, Settings::class.java)
            drawer.closeDrawer(GravityCompat.START)
            settingsIntent.putExtra("user", user)
            startActivity(settingsIntent, ActivityOptions.makeSceneTransitionAnimation(this@Learning).toBundle())
            supportFinishAfterTransition()
        }
    }
}