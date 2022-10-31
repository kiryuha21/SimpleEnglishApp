package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.simple_english.databinding.ActivityMainMenuBinding
import java.time.Duration

class MainMenu : AppCompatActivity() {
    lateinit var binding : ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setNavigationActions()
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> Toast.makeText(this@MainMenu, "education pressed", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this@MainMenu, "something else pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }
        navigation.settingsButton.setOnClickListener {
            startActivity(Intent(this@MainMenu, Settings::class.java),
                          ActivityOptions.makeSceneTransitionAnimation(this@MainMenu).toBundle())
        }
    }
}