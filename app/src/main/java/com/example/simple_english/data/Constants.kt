package com.example.simple_english.data

object Constants {
    const val registrationRequestCode = 0

    const val loginExtra = "login"
    const val passwordExtra = "password"

    const val debugURL = "http://localhost:8081"
    const val releaseURL = "https://simple-english-app.herokuapp.com"

    const val searchFailure = "No such user"
    const val wrongPassword = "Wrong password"
    const val success = "Success"
}

@kotlinx.serialization.Serializable
data class User(val id : Int, val username : String, val password : String)