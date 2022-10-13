package com.example.simple_english

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import com.example.simple_english.data.Constants
import com.google.android.material.textfield.TextInputEditText
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("HERE")
        if (requestCode == Constants.registrationRequestCode) {
            println("HERE TOO")
            val login = data?.getStringExtra(Constants.loginExtra)
            val password = data?.getStringExtra(Constants.passwordExtra)
            println(login + " " + password)

            val loginTV = findViewById<TextInputEditText>(R.id.login)
            val passwordTV = findViewById<TextInputEditText>(R.id.password)

            loginTV.setText(login)
            passwordTV.setText(password)
        }
    }

    fun onSignUpButtonClick(view : View) {
        val signUpIntent = Intent(this, SignUp::class.java)
        startActivityForResult(signUpIntent, Constants.registrationRequestCode)
    }
}
