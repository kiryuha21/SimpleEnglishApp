package com.example.simple_english

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.databinding.ActivitySignUpBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private fun setLoadState(isActive: Boolean) = with(binding) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEditOnChange(binding.userLogin)
        setEditOnChange(binding.userPassword)
        setEditOnChange(binding.userPasswordRepeat)
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
                    binding.userPasswordRepeatLayout.error = when (signUpResult) {
                        Constants.differentPasswords -> getString(R.string.different_passwords)
                        else -> null
                    }
                    binding.userLoginLayout.error = when (signUpResult) {
                        Constants.addError -> getString(R.string.user_exists)
                        Constants.badPattern -> getString(R.string.wrong_login_format)
                        else -> null
                    }
                }
            }
        }
    }

    private fun validUsername(text: String): Boolean {
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