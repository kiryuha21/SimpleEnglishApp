package com.example.simple_english.lib

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import com.example.simple_english.data.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Context.hideKeyboard(view: View) {
    val inputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun setEditOnChange(editText: TextInputEditText) {
    editText.apply {
        addTextChangedListener {
            (parent.parent as TextInputLayout).error = null
        }
    }
}

fun getPostBodyForUserXpUpdate(user: User, appendedXP: Int, completedTask: Int?): Map<String, String> {
    user.XP += appendedXP
    user.password = ""

    if (completedTask != null) {
        user.completedTasks += completedTask
    }

    val jsonUser = Json.encodeToString(user)
    return mapOf("id" to user.id.toString(), "stringUser" to jsonUser)
}