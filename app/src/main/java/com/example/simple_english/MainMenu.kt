package com.example.simple_english

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import com.example.simple_english.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity() {
    lateinit var binding : ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }
}