package com.example.simple_english

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class MainActivity : AppCompatActivity() {
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

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
        hideKeyboard(view)
        var authResult = Constants.searchFailure
        lifecycleScope.launch(Dispatchers.IO) {
            authResult = authHandling()
        }.invokeOnCompletion {
            if (authResult == Constants.success) {
                val mainMenuIntent = Intent(this, MainMenu::class.java)
                startActivity(mainMenuIntent)
            } else {
                runOnUiThread {
                    val errorTV = findViewById<TextView>(R.id.wrongPassTV)
                    if (authResult == Constants.searchFailure) {
                        errorTV.text = getText(R.string.no_such_user)
                    } else {
                        errorTV.text = getText(R.string.wrong_password)
                    }
                    errorTV.visibility = View.VISIBLE
                }
            }
        }
    }

    private suspend fun authHandling() : String {
        val loginTV = findViewById<TextInputEditText>(R.id.login)
        val login = loginTV.text.toString()

        val passwordTV = findViewById<TextInputEditText>(R.id.password)
        val password = passwordTV.text.toString()

        val requests = HttpRequests()
        val response = requests.sendAsyncPost("/get_by_name", mapOf("username" to login))
        if (response.isEmpty()) {
            return Constants.searchFailure
        }

        val user = Json.decodeFromString<User>(response)
        if (user.username == login && user.password == password) {
            return Constants.success
        }
        return Constants.wrongPassword
    }
}
