package com.example.simple_english

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {
    private lateinit var loginTV: EditText
    private lateinit var passwordTV: EditText
    private lateinit var passwordRepeatTV : EditText
    private lateinit var spinner: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var signUpButton: Button

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setLoadState(isActive : Boolean) {
        progressBar.visibility = when (isActive) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        loginTV.isEnabled = !isActive
        passwordTV.isEnabled = !isActive
        passwordRepeatTV.isEnabled = !isActive
        spinner.isEnabled = !isActive
        signUpButton.isEnabled = !isActive
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        loginTV = findViewById(R.id.userLogin)
        passwordTV = findViewById(R.id.userPassword)
        passwordRepeatTV = findViewById(R.id.userPasswordRepeat)
        progressBar = findViewById(R.id.sign_up_progress)
        spinner = findViewById(R.id.spinner)
        signUpButton = findViewById(R.id.sign_up_button)
    }

    fun onSignUpButtonClick(view: View) {
        hideKeyboard(view)
        var signUpResult = Constants.addError
        setLoadState(true)
        lifecycleScope.launch(Dispatchers.IO) {
            signUpResult = signUpHandling()
        }.invokeOnCompletion {
            runOnUiThread {
                setLoadState(false)

                if (signUpResult == Constants.success) {
                    intent.putExtra(Constants.loginExtra, loginTV.text.toString())
                    intent.putExtra(Constants.passwordExtra, passwordTV.text.toString())
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    val errorTV = findViewById<TextView>(R.id.errorTV)
                    errorTV.text = when (signUpResult) {
                        Constants.differentPasswords -> getString(R.string.different_passwords)
                        Constants.addError -> getString(R.string.user_exists)
                        else -> getString(R.string.wrong_login_format)
                    }
                    errorTV.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun validUsername(text : String): Boolean {
        val regex = Regex(".+@.+\\..+")
        return text.matches(regex)
    }

    private suspend fun signUpHandling(): String {
        val passwordRepeatString = passwordRepeatTV.text.toString()
        val passwordString = passwordTV.text.toString()
        if (passwordRepeatString != passwordString) {
            return Constants.differentPasswords
        }

        val usernameString = loginTV.text.toString()
        if (!validUsername(usernameString)) {
            return Constants.badPattern
        }

        val requests = HttpRequests()
        val response = requests.sendAsyncPost(
            "/add",
            mapOf("username" to usernameString, "password" to passwordString)
        )
        if (response.isEmpty()) {
            return Constants.addError
        }

        return Constants.success
    }
}