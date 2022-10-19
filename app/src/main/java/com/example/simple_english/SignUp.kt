package com.example.simple_english

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.ActivitySignUpBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setLoadState(isActive : Boolean) {
        binding.apply {
            signUpProgress.visibility = when (isActive) {
                true -> View.VISIBLE
                false -> View.GONE
            }

            userLogin.isEnabled = !isActive
            userPassword.isEnabled = !isActive
            userPasswordRepeat.isEnabled = !isActive
            spinner.isEnabled = !isActive
            signUpButton.isEnabled = !isActive
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                    intent.putExtra(Constants.loginExtra, binding.userLogin.text.toString())
                    intent.putExtra(Constants.passwordExtra, binding.userPassword.text.toString())
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    binding.userLogin.error = when (signUpResult) {
                        Constants.differentPasswords -> getString(R.string.different_passwords)
                        Constants.addError -> getString(R.string.user_exists)
                        else -> getString(R.string.wrong_login_format)
                    }
                }
            }
        }
    }

    private fun validUsername(text : String): Boolean {
        val regex = Regex(".+@.+\\..+")
        return text.matches(regex)
    }

    private suspend fun signUpHandling(): String {
        val passwordRepeatString = binding.userPasswordRepeat.text.toString()
        val passwordString = binding.userPassword.text.toString()
        if (passwordRepeatString != passwordString) {
            return Constants.differentPasswords
        }

        val usernameString = binding.userLogin.text.toString()
        if (!validUsername(usernameString)) {
            return Constants.badPattern
        }

        val requests = HttpsRequests()
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