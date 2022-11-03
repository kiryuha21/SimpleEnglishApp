package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import com.example.simple_english.data.Constants
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityLearningBinding

class Learning : AppCompatActivity() {
    private lateinit var binding: ActivityLearningBinding
    private lateinit var user: User
    private lateinit var learningType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("user") as User
        learningType = intent.getStringExtra("learning_type")!!
        binding.learningTypeTV.text = learningType

        supportFragmentManager.
            beginTransaction().
            replace(R.id.fragmentContainer, when(learningType) {
                Constants.reading -> Reading()
                Constants.theory -> Theory()
                Constants.audio -> Audio()
                else -> InsertWords()
            })
            .commit()

        setNavigationActions()
        setNavHeaderText()
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