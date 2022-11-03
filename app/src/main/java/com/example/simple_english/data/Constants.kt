package com.example.simple_english.data

import kotlinx.serialization.Serializable
import java.io.Serializable as Serial

object Constants {
    const val loginExtra = "login"
    const val passwordExtra = "password"

    const val debugURL = "http://localhost:8081"
    const val releaseURL = "https://simple-english-app.herokuapp.com"

    // Auth response bodies
    const val unknownError = "Unknown error"
    const val searchFailure = "No such user"
    const val wrongPassword = "Wrong password"
    const val success = "Success"

    // Registration constants
    const val addError = "Cannot add user"
    const val badPattern = "Wrong login format!"
    const val differentPasswords = "Passwords should match!"

    // Settings editing constants
    const val noChanges = "Nothing changed"
    const val passwordRequired = "Password required for profile changes"

    // Education forms
    const val reading = "Reading"
    const val theory = "Theory"
    const val insertWords = "Insert Words"
    const val audio = "Audio"
}

enum class HttpMethods {
    GET,
    POST,
    PUT
}

@Serializable
data class User(val id : Int, var username : String, var password : String, var name : String? = null) : Serial