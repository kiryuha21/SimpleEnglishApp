package com.example.simple_english

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.simple_english.data.Constants

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }

    fun onSignUpButtonClick(view : View)  {
        val loginTV = findViewById<EditText>(R.id.userLogin)
        val passwordTV = findViewById<EditText>(R.id.userPassword)

        intent.putExtra(Constants.loginExtra, loginTV.text.toString())
        intent.putExtra(Constants.passwordExtra, passwordTV.text.toString())
        setResult(RESULT_OK, intent)
        finish()
    }
}