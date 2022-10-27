package com.example.simple_english.data

object Constants {
    const val loginExtra = "login"
    const val passwordExtra = "password"

    const val debugURL = "http://localhost:8081"
    const val releaseURL = "https://simple-english-app.herokuapp.com"

    // Response bodies
    const val unknownError = "Unknown error"
    const val searchFailure = "No such user"
    const val wrongPassword = "Wrong password"
    const val addError = "Cannot add user"
    const val badPattern = "Wrong login format!"
    const val differentPasswords = "Passwords should match!"
    const val success = "Success"
}

@kotlinx.serialization.Serializable
data class User(val id : Int, val username : String, val password : String)