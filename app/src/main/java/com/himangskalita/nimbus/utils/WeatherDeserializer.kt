package com.himangskalita.nimbus.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.himangskalita.nimbus.data.Weather
import java.lang.reflect.Type

class WeatherDeserializer : JsonDeserializer<Weather> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Weather {

        val jsonData = json.asJsonObject

        val current = jsonData["current"].asJsonObject
        val daily = jsonData["daily"].asJsonObject

        return Weather(

            temperature_2m = current["temperature_2m"].asDouble,
            relative_humidity_2m = current["relative_humidity_2m"].asInt,
            rain = current["rain"].asDouble,
            weather_code = current["weather_code"].asInt,
            wind_speed_10m = current["wind_speed_10m"].asDouble,
            temperature_2m_max = daily["temperature_2m_max"].asJsonArray[0].asDouble,
            temperature_2m_min = daily["temperature_2m_min"].asJsonArray[0].asDouble
        )
    }
}