package com.example.simple_english

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }

    fun onSignUpButtonClick(view : View)  {
        hideKeyboard(view)
        var signUpResult = Constants.addError
        lifecycleScope.launch(Dispatchers.IO) {
            signUpResult = signUpHandling()
        }.invokeOnCompletion {
            if (signUpResult == Constants.success) {
                val loginTV = findViewById<EditText>(R.id.userLogin)
                val passwordTV = findViewById<EditText>(R.id.userPassword)

                intent.putExtra(Constants.loginExtra, loginTV.text.toString())
                intent.putExtra(Constants.passwordExtra, passwordTV.text.toString())
                setResult(RESULT_OK, intent)
                finish()
            } else {
                runOnUiThread {
                    val errorTV = findViewById<TextView>(R.id.errorTV)
                    if (signUpResult == Constants.differentPasswords) {
                        errorTV.text = getString(R.string.different_passwords)
                    } else {
                        errorTV.text = getString(R.string.user_exists)
                    }
                    errorTV.visibility = View.VISIBLE
                }
            }
        }
    }

    private suspend fun signUpHandling(): String {
        val password = findViewById<TextView>(R.id.userPassword).text.toString()
        val passwordRepeat = findViewById<TextView>(R.id.userPasswordRepeat).text.toString()
        if (passwordRepeat != password) {
            return Constants.differentPasswords
        }

        val username = findViewById<TextView>(R.id.userLogin).text.toString()

        val requests = HttpRequests()
        val response = requests.sendAsyncPost("/add", mapOf("username" to username, "password" to password))
        if (response.isEmpty()) {
            return Constants.addError
        }

        return Constants.success
    }
}