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
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityMainMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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

    private fun setNavHeaderText() {
        val navHeader = binding.navigation.commonNavigation.getHeaderView(0)
        val userGreetTV = navHeader.findViewById<TextView>(R.id.nav_header_greeting)
        userGreetTV.text = String.format(getText(R.string.nav_header_greeting).toString(), user.name ?: "Гость")
    }

    fun onTheoryCardClicked(view: View) {
        
    }

    fun onInsertWordsCardsClicked(view: View) {

    }

    fun onReadingCardClicked(view: View) {

    }

    fun onAudioCardClicked(view: View) {

    }

    override fun onRestart() {
        super.onRestart()
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonUser = HttpsRequests().sendAsyncRequest("/find_by_id", mapOf("id" to user.id.toString()), HttpMethods.POST)
            user = Json.decodeFromString(jsonUser)
        }.invokeOnCompletion {
            runOnUiThread {
                setNavHeaderText()
                binding.mainMenuWelcomeText.text = String.format(getText(R.string.welcome_text).toString(), user.name ?: "Гость")
            }
        }
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> Toast.makeText(this@MainMenu, "already here", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this@MainMenu, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }

        navigation.settingsButton.setOnClickListener {
            val settingsIntent = Intent(this@MainMenu, Settings::class.java)
            settingsIntent.putExtra("user", user)
            drawer.closeDrawer(GravityCompat.START)
            startActivity(settingsIntent, ActivityOptions.makeSceneTransitionAnimation(this@MainMenu).toBundle())
        }
    }
}