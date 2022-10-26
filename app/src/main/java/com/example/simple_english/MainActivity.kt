package com.example.simple_english

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class MainActivity : AppCompatActivity() {
    private val requests = HttpsRequests()
    private lateinit var binding: ActivityMainBinding

    private var registrationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val login = it.data?.getStringExtra(Constants.loginExtra)
                val password = it.data?.getStringExtra(Constants.passwordExtra)

                binding.login.setText(login)
                binding.password.setText(password)
            }
        }

    private fun setLoadState(isActive: Boolean) = with(binding) {
        signInProgress.visibility = when (isActive) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        login.isEnabled = !isActive
        password.isEnabled = !isActive
        signInButton.isEnabled = !isActive
        SignUpButton.isEnabled = !isActive
        forgotPasswordButton.isEnabled = !isActive
    }

    override fun onRestart() {
        super.onRestart()
        requests.sendEmptyRequest()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requests.sendEmptyRequest()

        setEditOnChange(binding.login)
        setEditOnChange(binding.password)
    }

    fun onSignUpButtonClick(view: View) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, binding.loginLayout, "login")
        registrationLauncher.launch(Intent(this, SignUp::class.java), options)
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
                    if (authResult == Constants.searchFailure) {
                        binding.loginLayout.error = getText(R.string.no_such_user)
                    } else {
                        binding.passwordLayout.error = getText(R.string.wrong_password)
                    }
                }
            }
        }
    }

    private suspend fun authHandling(): String {
        val login = binding.login.text.toString()
        val password = binding.password.text.toString()

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
