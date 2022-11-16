package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
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
    private val requests = HttpsRequests()
    private lateinit var learningType: String
    private var doubleBackToExitPressedOnce = false
    private val taskModel: TaskModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("user") as User
        taskModel.user.value = user
        learningType = intent.getStringExtra("learning_type")!!
        taskModel.currentType.value = learningType
        binding.learningTypeTV.text = learningType

        setNavigationActions()
        setNavHeaderText()

        if (!requests.isNetworkAvailable(this)) {
            Toast.makeText(this, getText(R.string.connect_and_reload), Toast.LENGTH_SHORT).show()
            return
        }

        when (learningType) {
            Constants.translator -> showTranslator()
            Constants.statistics -> showStatistics()
            else -> setTasks(learningType)
        }
    }

    private fun showTranslator() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, Translator())
            .commit()
    }

    private fun showStatistics() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, Statistics())
            .commit()
    }

    private fun setTasks(learningType: String) {
        binding.fragmentLoadingProgress.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            taskModel.tasks.postValue(
                Json.decodeFromString<ArrayList<TaskHeader>>(
                    if (learningType != Constants.memorising)
                        loadFromDB(learningType)
                    else
                        loadMemorising()
                )
            )
        }.invokeOnCompletion {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, ChooseTask())
                .commit()

            runOnUiThread {
                binding.fragmentLoadingProgress.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getText(R.string.back_button_double_press), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    private suspend fun loadMemorising() : String {
        return requests.sendAsyncRequest("/get_noticeable_headers", mapOf("id" to user.id.toString()), HttpMethods.POST)
    }

    private suspend fun loadFromDB(type: String): String {
        return requests.sendAsyncRequest("/find_task_headers_by_type", mapOf("type" to type), HttpMethods.POST)
    }

    private fun setNavHeaderText() {
        val navHeader = binding.navigation.commonNavigation.getHeaderView(0)
        val userGreetTV = navHeader.findViewById<TextView>(R.id.nav_header_greeting)
        userGreetTV.text = String.format(getText(R.string.nav_header_greeting).toString(), user.name ?: "Гость")
        val userXpTV = navHeader.findViewById<TextView>(R.id.nav_header_xp)
        userXpTV.text = String.format(getText(R.string.xp_progress).toString(), user.XP)
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
                R.id.memorising -> {
                    learningType = Constants.memorising
                    binding.learningTypeTV.text = learningType
                    taskModel.currentType.value = Constants.memorising
                    drawer.closeDrawer(GravityCompat.START)
                    setTasks(learningType)
                }
                R.id.translator -> {
                    learningType = Constants.translator
                    binding.learningTypeTV.text = learningType
                    taskModel.currentType.value = Constants.translator
                    drawer.closeDrawer(GravityCompat.START)
                    showTranslator()
                }
                R.id.statistics -> {
                    learningType = Constants.statistics
                    binding.learningTypeTV.text = learningType
                    taskModel.currentType.value = Constants.translator
                    drawer.closeDrawer(GravityCompat.START)
                    showStatistics()
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