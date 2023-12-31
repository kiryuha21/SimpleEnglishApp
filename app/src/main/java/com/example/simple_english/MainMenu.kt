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
import androidx.core.view.GravityCompat
import com.example.simple_english.data.Constants
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity() {
    lateinit var binding : ActivityMainMenuBinding
    private lateinit var user : User
    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getText(R.string.back_button_double_press), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setNavigationActions()

        user = intent.getSerializableExtra("user") as User

        binding.mainMenuWelcomeText.text = String.format(getText(R.string.welcome_text).toString(), user.name ?: "Гость")
        setNavHeaderText()
    }

    // sets text for navigation header
    private fun setNavHeaderText() {
        val navHeader = binding.navigation.commonNavigation.getHeaderView(0)
        val userGreetTV = navHeader.findViewById<TextView>(R.id.nav_header_greeting)
        userGreetTV.text = String.format(getText(R.string.nav_header_greeting).toString(), user.name ?: "Гость")
        val userXpTV = navHeader.findViewById<TextView>(R.id.nav_header_xp)
        userXpTV.text = String.format(getText(R.string.xp_progress).toString(), user.XP)
    }

    // common function to start education activity
    private fun learningActivityStart(learningType: String) {
        val learningIntent = Intent(this, Learning::class.java)
        learningIntent.putExtra("learning_type", learningType)
        learningIntent.putExtra("user", user)
        startActivity(learningIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        supportFinishAfterTransition()
    }

    fun onTheoryCardClicked(view: View) {
        learningActivityStart(Constants.theory)
    }

    fun onInsertWordsCardsClicked(view: View) {
        learningActivityStart(Constants.insertWords)
    }

    fun onReadingCardClicked(view: View) {
        learningActivityStart(Constants.reading)
    }

    fun onAudioCardClicked(view: View) {
        learningActivityStart(Constants.audio)
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> Toast.makeText(this@MainMenu, getText(R.string.already_here), Toast.LENGTH_SHORT).show()
                R.id.memorising -> learningActivityStart(Constants.memorising)
                R.id.translator -> learningActivityStart(Constants.translator)
                R.id.statistics -> learningActivityStart(Constants.statistics)
                else -> Toast.makeText(this@MainMenu, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }

        navigation.settingsButton.setOnClickListener {
            val settingsIntent = Intent(this@MainMenu, Settings::class.java)
            settingsIntent.putExtra("user", user)
            drawer.closeDrawer(GravityCompat.START)
            startActivity(settingsIntent, ActivityOptions.makeSceneTransitionAnimation(this@MainMenu).toBundle())
            supportFinishAfterTransition()
        }
    }
}