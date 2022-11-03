package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.example.simple_english.databinding.ActivityLearningBinding

class Learning : AppCompatActivity() {
    lateinit var binding: ActivityLearningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.learningTypeTV.text = intent.getStringExtra("learning_type")
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {  // TODO: should be finished
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> Toast.makeText(this@Learning, "already here", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this@Learning, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }

        navigation.settingsButton.setOnClickListener {
            val settingsIntent = Intent(this@Learning, Settings::class.java)
            drawer.closeDrawer(GravityCompat.START)
            startActivity(settingsIntent, ActivityOptions.makeSceneTransitionAnimation(this@Learning).toBundle())
            supportFinishAfterTransition()
        }
    }
}