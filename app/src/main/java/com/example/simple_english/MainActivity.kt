package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.*
import com.example.simple_english.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {
    private val requests = HttpsRequests()
    private lateinit var binding: ActivityMainBinding
    private lateinit var user : User

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
                    val menuIntent = Intent(this, MainMenu::class.java)
                    menuIntent.putExtra("user", user)
                    startActivity(menuIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                    finish()
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

        val response = requests.sendAsyncRequest("/auth", mapOf("username" to login, "password" to password), HttpMethods.POST)
        return when(response) {
            Constants.success -> {
                val jsonUser = requests.sendAsyncRequest("/find_by_username", mapOf("username" to login), HttpMethods.POST)
                println(jsonUser)
                user = Json.decodeFromString(jsonUser)
                println(user)
                Constants.success
            }
            "" -> Constants.unknownError
            else -> response
        }
    }
}
