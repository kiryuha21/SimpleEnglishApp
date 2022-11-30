package com.example.simple_english

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.databinding.ActivityPasswordResetBinding
import com.example.simple_english.lib.HttpsRequests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PasswordReset : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordResetBinding
    private lateinit var user: User
    private val requests = HttpsRequests()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // enables blocks after certain correct user input
    private fun enableBlocks(first: Boolean, second: Boolean) {
        binding.secretWordEditLayout.isEnabled = first
        binding.secretWordConfirmButton.isEnabled = first

        binding.newPasswordEditLayout.isEnabled = second
        binding.newPasswordSaveButton.isEnabled = second
    }

    fun onFindAccountClicked(view: View) {
        binding.resetPasswordLoading.visibility = View.VISIBLE

        lateinit var jsonUser: String
        lifecycleScope.launch(Dispatchers.IO) {
            jsonUser = requests.sendAsyncRequest("/find_user_by_username", mapOf("username" to binding.userLoginEdit.text.toString()), HttpMethods.POST)
        }.invokeOnCompletion {
            runOnUiThread {
                if (jsonUser.isEmpty()) {
                    Toast.makeText(this, "Пользователь не найден!", Toast.LENGTH_SHORT).show()
                    enableBlocks(first = false, second = false)
                } else {
                    user = Json.decodeFromString(jsonUser)
                    binding.secretTypeTV.text = String.format(getText(R.string.your_secret_type_placeholder).toString(), user.secretWordType)
                    enableBlocks(first = true, second = false)
                }
                binding.resetPasswordLoading.visibility = View.GONE
            }
        }
    }

    fun onSecretConfirmedClicked(view: View) {
        if (binding.secretWordEdit.text.toString() != user.secretWord) {
            Toast.makeText(this, "Неправильное секретное слово!", Toast.LENGTH_SHORT).show()
            return
        }

        enableBlocks(first = true, second = true)
    }

    fun onBackClicked(view: View) {
        val signInIntent = Intent(this, SignIn::class.java)
        startActivity(signInIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        supportFinishAfterTransition()
    }

    fun onPasswordSaveClicked(view: View) {
        binding.resetPasswordLoading.visibility = View.VISIBLE

        user.password = binding.newPasswordEdit.text.toString()
        val jsonUser = Json.encodeToString(user)
        lifecycleScope.launch(Dispatchers.IO) {
            requests.sendAsyncRequest("/update_user", mapOf("id" to user.id.toString(), "stringUser" to jsonUser), HttpMethods.PUT)
        }.invokeOnCompletion {
            runOnUiThread {
                binding.resetPasswordLoading.visibility = View.GONE
                Toast.makeText(this, "Пароль был успешно обновлен!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}