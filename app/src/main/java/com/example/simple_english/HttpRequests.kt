package com.example.simple_english

import com.example.simple_english.data.Constants
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit

class HttpRequests {
    private val activeUrlBase = Constants.releaseURL
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    suspend fun sendAsyncPost(option: String, body: Map<String, String>): String {
        val result : String

        val postBody = FormBody.Builder()
        for ((key, value) in body) {
            postBody.add(key, value)
        }
        val postData = postBody.build()

        val request = Request.Builder()
            .url(activeUrlBase + option)
            .post(postData)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val respBody = response.body!!.string()
            result = if (respBody == "") Constants.searchFailure else respBody
        }

        return result
    }
}