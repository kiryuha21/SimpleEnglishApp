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
import com.example.simple_english.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setLoadState(isActive : Boolean) {
        binding.signInProgress.visibility = when (isActive) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        binding.login.isEnabled = !isActive
        binding.password.isEnabled = !isActive
        binding.signInButton.isEnabled = !isActive
        binding.SignUpButton.isEnabled = !isActive
        binding.forgotPasswordButton.isEnabled = !isActive
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.registrationRequestCode) {
            val login = data?.getStringExtra(Constants.loginExtra)
            val password = data?.getStringExtra(Constants.passwordExtra)

            binding.login.setText(login)
            binding.password.setText(password)
        }
    }

    fun onSignUpButtonClick(view: View) {
        val signUpIntent = Intent(this, SignUp::class.java)
        startActivityForResult(signUpIntent, Constants.registrationRequestCode)
    }

    fun onSignInButtonClick(view: View) {
        hideKeyboard(view)
        var authResult = Constants.searchFailure

        setLoadState(true)
        lifecycleScope.launch(Dispatchers.IO) {
            authResult = authHandling()
        }.invokeOnCompletion {
            runOnUiThread {
                setLoadState(false)

                if (authResult == Constants.success) {
                    val mainMenuIntent = Intent(this, MainMenu::class.java)
                    startActivity(mainMenuIntent)
                } else {
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

    private suspend fun authHandling(): String {
        val login = binding.login.text.toString()
        val password = binding.password.text.toString()

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
