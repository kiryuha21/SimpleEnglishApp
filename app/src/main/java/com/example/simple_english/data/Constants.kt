package com.example.simple_english.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.sql.Timestamp
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
    const val memorising = "Запоминание"

    // Extra "education" forms
    const val translator = "Переводчик"
    const val statistics = "Статистика"

    // Transition names
    const val taskHeaderTransitionName = "taskHeader%d"

    // Adapter
    const val doneTask = -1
    const val doneColor = "#D3D3D3"

    // Insert Words
    const val correctAnswer = "<font color=green>%s</font>"
    const val wrongAnswer = "<font color=red>%s</font>"

    // Memorising
    const val memoFinished = "'1000 years'"
}

enum class HttpMethods {
    GET,
    POST,
    PUT
}

object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor = PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Timestamp {
        return Timestamp.valueOf(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class User(val id: Int,
                var username : String,
                var password : String,
                var secretWord : String,
                var secretWordType: String,
                var XP : Int,
                var completedTasks : IntArray,
                var startedMemories : IntArray,
                var name : String? = null) : Serial

@Serializable
data class TaskContent(val id: Int?,
                       val taskText: String?,
                       val taskVariants: Array<Array<String?>?>?,
                       val correctVariants: Array<String?>?,
                       val questions: Array<String?>?,
                       @Serializable(with = TimestampSerializer::class)
                       var memLastUpdate: Timestamp?,
                       var nextNoticeIn : String?,
                       val musicURL: String?)

@Serializable
data class TaskHeader(val id: Int?,
                      val taskType: String,
                      var pointsXP: Int,
                      val description: String,
                      val content: TaskContent)
