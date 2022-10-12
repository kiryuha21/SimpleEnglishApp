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
    //val db : DatabaseUsage = DatabaseUsage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*db.connectToDatabase()
        transaction {
            for (city in DatabaseUsage.Users.selectAll()) {
                val testdb = findViewById<TextView>(R.id.app_name);
                testdb.text = "${city[DatabaseUsage.Users.id]}: ${city[DatabaseUsage.Users.password]}"
            }
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.registrationRequestCode) {
            val login = data?.getStringExtra("login")
            val password = data?.getStringExtra("password").toString()

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
