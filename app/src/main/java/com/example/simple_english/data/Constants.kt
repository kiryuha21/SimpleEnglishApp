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
    const val reading = "Чтение"
    const val theory = "Теория"
    const val insertWords = "Вставьте слова"
    const val audio = "Аудирование"

    // Transition names
    const val taskHeaderTransitionName = "taskHeader%d"

    // Adapter
    const val doneTask = -1
    const val doneColor = "#D3D3D3"

    // Insert Words
    const val correctAnswer = "<font color=green>%s</font>"
    const val wrongAnswer = "<font color=red>%s</font>"
}

enum class HttpMethods {
    GET,
    POST,
    PUT
}

@Serializable
data class User(val id: Int,
                var username : String,
                var password : String,
                var XP : Int,
                var completedTasks : IntArray,
                var name : String? = null) : Serial

@Serializable
data class TaskContent(val id: Int,
                       val taskText: String,
                       val taskVariants: Array<Array<String>>,
                       val correctVariants: Array<String>)

@Serializable
data class TaskHeader(val id: Int,
                      val taskType: String,
                      var pointsXP: Int,
                      val description: String,
                      val content: TaskContent)
