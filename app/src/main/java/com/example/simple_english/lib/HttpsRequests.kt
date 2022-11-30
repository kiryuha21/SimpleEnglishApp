package com.example.simple_english.lib

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import kotlinx.serialization.json.*
import okhttp3.*
import okio.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class HttpsRequests {
    private val activeUrlBase = Constants.releaseURL
    private val musicBaseUrl = "https://cloud-api.yandex.net/v1/disk/public/resources/download?"
    private val client = OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun formRequest(option: String, data: FormBody, method: HttpMethods): Request =
        when (method) {
            HttpMethods.POST -> Request.Builder().url(activeUrlBase + option).post(data).build()
            HttpMethods.PUT -> Request.Builder().url(activeUrlBase + option).put(data).build()
            HttpMethods.GET -> Request.Builder().url(activeUrlBase + option).get().build()
        }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork // network is currently in a high power state for performing data transmission.

        // return false if network is null
        network ?: return false

        // return false if Network Capabilities is null
        val actNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> { // check if wifi is connected
                true
            }
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> { // check if mobile dats is connected
                true
            }
            else -> {
                false
            }
        }
    }

    // Sends request with given params
    suspend fun sendAsyncRequest(
        option: String,
        body: Map<String, String>,
        method: HttpMethods
    ): String {
        val result: String

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

    // sends async GET request to turn on server
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

    // forms direct URL for music downloading
    suspend fun getMusicFileUrl(publicKey: String): String {
        val result: String
        val finalUrl = musicBaseUrl + "public_key=${URLEncoder.encode(publicKey, "utf-8")}"
        val request = Request.Builder().url(finalUrl).get().build()

        client.newCall(request).execute().use { response ->
            result = when (response.isSuccessful) {
                true -> {
                    val json = Json.parseToJsonElement(response.body!!.string())
                    json.jsonObject["href"]?.toString()?.trim('"') ?: ""
                }
                false -> ""
            }
        }
        return result
    }

    // Sends GET request with given headers
    suspend fun getWithHeaders(url: String, headers: Map<String, String>): JsonElement {
        val headerBuilder = Headers.Builder()
        for ((key, value) in headers) {
            headerBuilder.add(key, value)
        }
        val readyHeaders = headerBuilder.build()

        val request = Request.Builder().url(url).headers(readyHeaders).get().build()

        lateinit var result: JsonElement
        client.newCall(request).execute().use { response ->
            result = Json.parseToJsonElement(response.body!!.string())
        }
        return result
    }

    // Forms URL query from given map
    fun formQuery(map: Map<String, String>): String {
        return map.entries.stream()
            .map { (k, v) ->
                "${URLEncoder.encode(k, "utf-8")}=${URLEncoder.encode(v.toString(), "utf-8")}"
            }
            .reduce { p1, p2 -> "$p1&$p2" }
            .orElse("")
    }
}