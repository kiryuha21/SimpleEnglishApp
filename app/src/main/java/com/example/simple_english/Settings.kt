package com.example.simple_english

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivitySettingsBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.setEditOnChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Settings : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    private lateinit var user : User
    private val requests = HttpsRequests()

    private lateinit var developersAlert : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setNavigationActions()

        developersAlert = AlertDialog.Builder(this)
            .setTitle(getText(R.string.contact_developers))
            .setMessage(getText(R.string.developers_desc))
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()

        setEditOnChange(binding.settingsUserLogin)
        setEditOnChange(binding.settingsUserPassword)
        setEditOnChange(binding.settingsUserName)

        if (requests.isNetworkAvailable(this)) {
            requests.sendEmptyRequest()
        } else {
            Toast.makeText(this, getText(R.string.connect_to_internet), Toast.LENGTH_SHORT).show()
        }

        user = intent.getSerializableExtra("user") as User

        binding.settingsLoginTV.text = user.username
        binding.settingsNameTV.text = user.name

        binding.settingsUserLogin.setText(user.username)
        binding.settingsUserName.setText(user.name)

        setNavHeaderText()
    }

    private fun setNavHeaderText() {
        val navHeader = binding.navigation.commonNavigation.getHeaderView(0)
        val userGreetTV = navHeader.findViewById<TextView>(R.id.nav_header_greeting)
        userGreetTV.text = String.format(getText(R.string.nav_header_greeting).toString(), user.name ?: "Гость")
        val userXpTV = navHeader.findViewById<TextView>(R.id.nav_header_xp)
        userXpTV.text = String.format(getText(R.string.xp_progress).toString(), user.XP)
    }

    fun onSaveButtonClick(view : View) {
        setLoadState(true)
        var updateResult = Constants.searchFailure

        if (!requests.isNetworkAvailable(this)) {
            Toast.makeText(this, getText(R.string.no_connection), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            updateResult = saveChangesHandling()
        }.invokeOnCompletion {
            runOnUiThread {
                setLoadState(false)
                if (updateResult == Constants.success) {
                    binding.settingsNameTV.text = user.name
                    binding.settingsLoginTV.text = user.username
                    binding.settingsUserPassword.setText("")
                    setNavHeaderText()
                    Toast.makeText(this, getText(R.string.successful_update), Toast.LENGTH_SHORT).show()
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

        val jsonUser = Json.encodeToString(User(user.id, login, password, user.secretWord, user.secretWordType, user.XP, user.completedTasks, user.startedMemories, name))

        user.name = name
        user.username = login
        return requests.sendAsyncRequest("/update_user", mapOf("id" to user.id.toString(), "stringUser" to jsonUser), HttpMethods.PUT)
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
        if (requests.isNetworkAvailable(this)) {
            requests.sendEmptyRequest()
        } else {
            Toast.makeText(this, getText(R.string.connect_to_internet), Toast.LENGTH_SHORT).show()
        }
    }

    fun onMenuImageClick(view: View) {
        binding.drawer.openDrawer(GravityCompat.START)
    }

    fun onExitButtonClick(view: View) {
        val mainIntent = Intent(applicationContext, SignIn::class.java)
        startActivity(mainIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        supportFinishAfterTransition()
    }

    fun onSupportButtonClick(view: View) {
        developersAlert.show()
    }

    private fun startIntent(_class: Class<*>, user: User, extras: Map<String, String>?) {
        val intent = Intent(this, _class)
        binding.drawer.closeDrawer(GravityCompat.START)

        intent.putExtra("user", user)
        if (extras != null) {
            for ((key, value) in extras) {
                intent.putExtra(key, value)
            }
        }

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        supportFinishAfterTransition()
    }

    private fun setNavigationActions() = with(binding) {
        navigation.commonNavigation.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.education -> startIntent(MainMenu::class.java, user, null)
                R.id.memorising -> startIntent(Learning::class.java, user, mapOf("learning_type" to Constants.memorising))
                R.id.translator -> startIntent(Learning::class.java, user, mapOf("learning_type" to Constants.translator))
                R.id.statistics -> startIntent(Learning::class.java, user, mapOf("learning_type" to Constants.statistics))
                else -> Toast.makeText(this@Settings, "something pressed", Toast.LENGTH_SHORT).show()
            }
            true
        }
        navigation.settingsButton.setOnClickListener {
            Toast.makeText(this@Settings, getText(R.string.already_here), Toast.LENGTH_SHORT).show()
        }
    }
}
