package com.himangskalita.nimbus.di.repository

import com.google.gson.GsonBuilder
import com.himangskalita.nimbus.data.Weather
import com.himangskalita.nimbus.utils.WeatherDeserializer
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton


interface WeatherRepository {

    fun fetchWeatherDataFromApi(queryParams: Map<String, String>): Result<Weather>
}

@Singleton
class WeatherRepositoryIml @Inject constructor(
    private val client: OkHttpClient
) : WeatherRepository {

    override fun fetchWeatherDataFromApi(queryParams: Map<String, String>): Result<Weather> {

        val httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("api.open-meteo.com")
            .addPathSegments("v1")
            .addPathSegments("forecast")
            .apply {

                queryParams.forEach { key, value ->

                    addQueryParameter(key, value)
                }
            }
            .build()

        val request = Request.Builder()
            .url(httpurl)
            .build()

        return try {

            client.newCall(request).execute().use { response ->

                if (!response.isSuccessful) {

                    return Result.failure(
                        Exception("HTTP error - Code: ${response.code}, message: ${response.message}")

                    )
                }

                val jsonResponse = response.body?.string().orEmpty()
                val gson = GsonBuilder()
                    .registerTypeAdapter(Weather::class.java, WeatherDeserializer())
                    .create()
                val weatherData = gson.fromJson(jsonResponse, Weather::class.java)

                Result.success(weatherData)
            }
        } catch (e: IOException) {

            Result.failure(e)
        }
    }
}