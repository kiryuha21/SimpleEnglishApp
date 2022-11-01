package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity() {
    lateinit var binding : ActivityMainMenuBinding

    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setNavigationActions()

        user = intent.getSerializableExtra("user") as User
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