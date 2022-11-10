package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.*
import com.example.simple_english.databinding.ActivityMainBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.hideKeyboard
import com.example.simple_english.lib.setEditOnChange
import kotlinx.coroutines.*
import androidx.core.util.Pair as UtilPair
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SignIn : AppCompatActivity() {
    private val requests = HttpsRequests()
    private lateinit var binding: ActivityMainBinding
    private lateinit var user : User
    private var doubleBackToExitPressedOnce = false

    private var registrationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val login = it.data?.getStringExtra(Constants.loginExtra)
                val password = it.data?.getStringExtra(Constants.passwordExtra)

                binding.login.setText(login)
                binding.password.setText(password)
            }
        }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getText(R.string.back_button_double_press), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
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
        if (requests.isNetworkAvailable(this)) {
            requests.sendEmptyRequest()
        } else {
            Toast.makeText(this, getText(R.string.connect_to_internet), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (requests.isNetworkAvailable(this)) {
            requests.sendEmptyRequest()
        } else {
            Toast.makeText(this, getText(R.string.connect_to_internet), Toast.LENGTH_SHORT).show()
        }

        setEditOnChange(binding.login)
        setEditOnChange(binding.password)
    }

    fun onSignUpButtonClick(view: View) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
            UtilPair.create(binding.loginLayout, "login"),
            UtilPair.create(binding.passwordLayout, "password"))
        registrationLauncher.launch(Intent(this, SignUp::class.java), options)
    }

    fun onSignInButtonClick(view: View) {
        hideKeyboard(view)
        var authResult = Constants.searchFailure

        if (!requests.isNetworkAvailable(this)) {
            Toast.makeText(this, getText(R.string.no_connection), Toast.LENGTH_SHORT).show()
            return
        }

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
                    supportFinishAfterTransition()
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

        val postBody = mapOf("username" to login, "password" to password)
        return when(val response = requests.sendAsyncRequest("/auth_user", postBody, HttpMethods.POST)) {
            Constants.success -> {
                val jsonUser = requests.sendAsyncRequest("/find_user_by_username", mapOf("username" to login), HttpMethods.POST)
                user = Json.decodeFromString(jsonUser)
                Constants.success
            }
            "" -> Constants.unknownError
            else -> response
        }
    }
}
