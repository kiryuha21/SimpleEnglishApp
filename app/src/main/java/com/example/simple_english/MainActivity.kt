package com.example.simple_english

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.registrationRequestCode) {
            val login = data?.getStringExtra(Constants.loginExtra)
            val password = data?.getStringExtra(Constants.passwordExtra)

            val loginTV = findViewById<TextInputEditText>(R.id.login)
            val passwordTV = findViewById<TextInputEditText>(R.id.password)

            loginTV.setText(login)
            passwordTV.setText(password)
        }
    }

    fun onSignUpButtonClick(view : View) {
        val signUpIntent = Intent(this, SignUp::class.java)
        startActivityForResult(signUpIntent, Constants.registrationRequestCode)
    }

    fun onSignInButtonClick(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            authHandling()
        }
        val mainMenuIntent = Intent(this, MainMenu::class.java)
    }

    private suspend fun authHandling() {
        val loginTV = findViewById<TextInputEditText>(R.id.login)
        val login = loginTV.text.toString()

        val passwordTV = findViewById<TextInputEditText>(R.id.password)
        val password = passwordTV.text.toString()

        val requests = HttpRequests()
        val response = requests.sendAsyncPost("/get_by_name", mapOf("username" to login))
        println("responce = $response")
    }
}
