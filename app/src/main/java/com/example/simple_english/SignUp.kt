package com.example.simple_english

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.databinding.ActivitySignUpBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.hideKeyboard
import com.example.simple_english.lib.setEditOnChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val requests = HttpsRequests()

    private fun setLoadState(isActive: Boolean) = with(binding) {
        signUpProgress.visibility = when (isActive) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        userLogin.isEnabled = !isActive
        userPassword.isEnabled = !isActive
        userPasswordRepeat.isEnabled = !isActive
        spinner.isEnabled = !isActive
        registerButton.isEnabled = !isActive
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

        if (!requests.isNetworkAvailable(this)) {
            Toast.makeText(this, getText(R.string.no_connection), Toast.LENGTH_SHORT).show()
            return
        }

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
                    supportFinishAfterTransition()
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

    // Checks if username is valid with regex
    private fun validUsername(text: String): Boolean {
        val regex = Regex(".+@.+\\..+")
        return text.matches(regex)
    }

    // forms request to sign up, sends it and returns response
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

        val secretWord = binding.userSecret.text.toString()
        val secretWordType = binding.spinner.selectedItem.toString()

        val response = requests.sendAsyncRequest(
            "/add_user",
            mapOf("username" to usernameString, "password" to passwordString, "secretWord" to secretWord, "secretWordType" to secretWordType),
            HttpMethods.POST
        )
        if (response.isEmpty()) {
            return Constants.addError
        }

        return Constants.success
    }
}