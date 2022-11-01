package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Settings : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    private lateinit var user : User
    private val requests = HttpsRequests()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setNavigationActions()

        setEditOnChange(binding.settingsUserLogin)
        setEditOnChange(binding.settingsUserPassword)
        setEditOnChange(binding.settingsUserName)

        requests.sendEmptyRequest()

        user = intent.getSerializableExtra("user") as User

        binding.settingsLoginTV.text = user.username
        binding.settingsNameTV.text = user.name

        binding.settingsUserLogin.setText(user.username)
        binding.settingsUserName.setText(user.name)
    }

    fun onSaveButtonClick(view : View) {
        setLoadState(true)
        var updateResult = Constants.searchFailure

        lifecycleScope.launch(Dispatchers.IO) {
            updateResult = saveChangesHandling()
        }.invokeOnCompletion {
            runOnUiThread {
                setLoadState(false)
                if (updateResult == Constants.success) {
                    binding.settingsNameTV.text = user.name
                    binding.settingsLoginTV.text = user.username
                    Toast.makeText(this, "Profile was successfully updated!", Toast.LENGTH_SHORT).show()
                } else {
                    binding.apply {
                        when (updateResult) {
                            Constants.searchFailure -> settingsUserLoginLayout.error = getText(R.string.no_such_user)
                            Constants.wrongPassword -> settingsUserPasswordLayout.error = getText(R.string.wrong_password)
                            Constants.noChanges -> Toast.makeText(this@Settings, Constants.noChanges, Toast.LENGTH_SHORT).show()
                            Constants.passwordRequired -> settingsUserPasswordLayout.error = getText(R.string.password_required)
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveChangesHandling(): String {
        val login = binding.settingsUserLogin.text.toString()
        val name = binding.settingsUserName.text.toString()
        val password = binding.settingsUserPassword.text.toString()

        if (user.name == name && user.username == login && password.isEmpty()) {
            return Constants.noChanges
        }

        val jsonUser = Json.encodeToString(User(user.id, login, password, name))

        user.name = name
        user.username = login
        return requests.sendAsyncRequest("/update", mapOf("id" to user.id.toString(), "stringUser" to jsonUser), HttpMethods.PUT)
    }

    private fun setLoadState(isActive: Boolean) = with(binding) {
        updateProgress.visibility = when (isActive) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        settingsUserLogin.isEnabled = !isActive
        settingsUserName.isEnabled = !isActive
        settingsUserPassword.isEnabled = !isActive
        settingsExitButton.isEnabled = !isActive
        settingsSaveButton.isEnabled = !isActive
        settingsSupportButton.isEnabled = !isActive
    }

    override fun onRestart() {
        super.onRestart()
        requests.sendEmptyRequest()
    }

    fun onMenuImageClick(view : View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> {
                    val educationIntent = Intent(this@Settings, MainMenu::class.java)
                    educationIntent.putExtra("user", user)
                    startActivity(educationIntent, ActivityOptions.makeSceneTransitionAnimation(this@Settings).toBundle())
                }
                else -> Toast.makeText(this@Settings, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }
        navigation.settingsButton.setOnClickListener {
            Toast.makeText(this@Settings, "already here", Toast.LENGTH_SHORT).show()
        }
    }
}
