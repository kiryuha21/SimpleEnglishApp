package com.example.simple_english.lib

import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import kotlinx.serialization.json.*
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit

class HttpsRequests {
    private val activeUrlBase = Constants.releaseURL
    private val musicBaseUrl = "https://cloud-api.yandex.net/v1/disk/public/resources/download?"
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendAsyncRequest(option: String, body: Map<String, String>, method: HttpMethods): String {
        val result : String

        val postBody = FormBody.Builder()
        for ((key, value) in body) {
            postBody.add(key, value)
        }
        val postData = postBody.build()

        val request = formRequest(option, postData, method)

        client.newCall(request).execute().use { response ->
            result = when (response.isSuccessful) {
                true -> response.body!!.string()
                false -> ""
            }
        }

        return result
    }

    private fun formRequest(option: String, data : FormBody, method: HttpMethods) : Request = when(method) {
        HttpMethods.POST -> Request.Builder().url(activeUrlBase + option).post(data).build()
        HttpMethods.PUT -> Request.Builder().url(activeUrlBase + option).put(data).build()
        HttpMethods.GET -> Request.Builder().url(activeUrlBase + option).get().build()
    }

    fun sendEmptyRequest() {
        val request = Request.Builder().url(activeUrlBase).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                }
            }
        })
    }

    suspend fun getMusicFileUrl(publicKey: String): String {
        val result: String
        val finalUrl = musicBaseUrl + "public_key=${java.net.URLEncoder.encode(publicKey, "utf-8")}"
        val request = Request.Builder().url(finalUrl).get().build()

        client.newCall(request).execute().use { response ->
            result = when (response.isSuccessful) {
                true -> {
                    val json = Json.parseToJsonElement(response.body!!.string())
                    json.jsonObject["href"]!!.toString().trim('"')
                }
                false -> ""
            }
        }
        return result
    }
}