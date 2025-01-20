package com.himangskalita.nimbus.data

data class Weather(
    val city: String = "City Name",
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val rain: Double,
    val weather_code: Int,
    val wind_speed_10m: Double,
    val temperature_2m_max: Double,
    val temperature_2m_min: Double
)

