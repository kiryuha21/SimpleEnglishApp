package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivitySettingsBinding

class Settings : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setNavigationActions()

        user = intent.getSerializableExtra("user") as User

        binding.settingsLoginTV.text = user.username
        binding.settingsNameTV.text = user.name

        binding.settingsUserLogin.setText(user.username)
        binding.settingsUserName.setText(user.name)
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> {
                    startActivity(
                        Intent(this@Settings, MainMenu::class.java),
                        ActivityOptions.makeSceneTransitionAnimation(this@Settings).toBundle())
                }
                else -> Toast.makeText(this@Settings, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }
        navigation.settingsButton.setOnClickListener {
            Toast.makeText(this@Settings, "already here", Toast.LENGTH_SHORT).show()
        }
    }
}