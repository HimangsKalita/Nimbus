package com.himangskalita.nimbus.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.himangskalita.nimbus.R
import com.himangskalita.nimbus.data.Weather
import com.himangskalita.nimbus.di.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(

    @ApplicationContext private val applicationContext: Context,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _weatherData = MutableStateFlow<Result<Weather>?>(null)
    val weatherData: StateFlow<Result<Weather>?>
        get() = _weatherData

    private var lastLocation: Location? = null

    @SuppressLint("MissingPermission")
    fun fetchLocationAndWeather() {

        LocationServices.getFusedLocationProviderClient(applicationContext).lastLocation.addOnSuccessListener { location ->

            location?.let {

                lastLocation = it
                fetchWeatherData(it)
            }
        }
    }

    private fun fetchWeatherData(location: Location) {

        viewModelScope.launch(Dispatchers.IO) {

            val queryParams = mapOf(
                "latitude" to location.latitude.toString(),
                "longitude" to location.longitude.toString(),
                "current" to "temperature_2m,relative_humidity_2m,rain,weather_code,wind_speed_10m",
                "daily" to "temperature_2m_max,temperature_2m_min"
            )

            _weatherData.value = weatherRepository.fetchWeatherDataFromApi(queryParams)
        }
    }

    fun getCityName(): String? {

        return lastLocation?.let {

            try {
                val geocoder = Geocoder(applicationContext, Locale.getDefault())

                val address =
                    geocoder.getFromLocation(lastLocation!!.latitude, lastLocation!!.longitude, 1)

                if (address != null) {

                    address[0].locality
                } else {

                    null
                }
            } catch (e: Exception) {

                null
            }
        }

    }

    fun getWeatherCondition(weatherCode: Int): WeatherCondition {

        return WeatherCondition.fromCode(weatherCode)
    }

    enum class WeatherCondition(
        val description: String,
        val emoji: String,
        val backgroundColor: Int
    ) {

        CLEAR_SKY("Clear sky", "☀️", R.color.lightBlue),
        MAINLY_CLEAR("Mainly clear", "🌤️", R.color.grey),
        PARTLY_CLOUDY("Partly cloudy", "⛅", R.color.grey),
        OVERCAST("Overcast", "☁️", R.color.grey),
        FOG("Fog", "🌫️", R.color.darkGrey),
        DEPOSITING_RIME_FOG("Depositing rime fog", "🌫️", R.color.darkGrey),
        LIGHT_DRIZZLE("Light drizzle", "🌦️", R.color.lightBlue),
        MODERATE_DRIZZLE("Moderate drizzle", "🌧️", R.color.lightBlue),
        DENSE_DRIZZLE("Dense drizzle", "🌧️", R.color.lightBlue),
        LIGHT_FREEZING_DRIZZLE("Light freezing drizzle", "❄️", R.color.lightBlue),
        DENSE_FREEZING_DRIZZLE("Dense freezing drizzle", "❄️", R.color.lightBlue),
        SLIGHT_RAIN("Slight rain", "🌦️", R.color.blue),
        MODERATE_RAIN("Moderate rain", "🌧️", R.color.blue),
        HEAVY_RAIN("Heavy rain", "🌧️", R.color.blue),
        LIGHT_FREEZING_RAIN("Light freezing rain", "❄️", R.color.blue),
        HEAVY_FREEZING_RAIN("Heavy freezing rain", "❄️", R.color.blue),
        SLIGHT_SNOW("Slight snow", "🌨️", R.color.white),
        MODERATE_SNOW("Moderate snow", "🌨️", R.color.white),
        HEAVY_SNOW("Heavy snow", "🌨️", R.color.white),
        SNOW_GRAINS("Snow grains", "🌨️", R.color.white),
        SLIGHT_RAIN_SHOWERS("Slight rain showers", "🌦️", R.color.blue),
        MODERATE_RAIN_SHOWERS("Moderate rain showers", "🌧️", R.color.blue),
        VIOLENT_RAIN_SHOWERS("Violent rain showers", "🌧️", R.color.blue),
        SLIGHT_SNOW_SHOWERS("Slight snow showers", "❄️", R.color.white),
        HEAVY_SNOW_SHOWERS("Heavy snow showers", "❄️", R.color.white),
        SLIGHT_THUNDERSTORM("Slight thunderstorm", "⛈️", R.color.darkGrey),
        THUNDERSTORM_WITH_SLIGHT_HAIL("Thunderstorm with slight hail", "⛈️", R.color.darkGrey),
        THUNDERSTORM_WITH_HEAVY_HAIL("Thunderstorm with heavy hail", "⛈️", R.color.darkGrey),
        UNKNOWN("Unknown", "❓", R.color.white);

        companion object {

            fun fromCode(code: Int): WeatherCondition = when (code) {

                0 -> CLEAR_SKY
                1 -> MAINLY_CLEAR
                2 -> PARTLY_CLOUDY
                3 -> OVERCAST
                45 -> FOG
                48 -> DEPOSITING_RIME_FOG
                51 -> LIGHT_DRIZZLE
                53 -> MODERATE_DRIZZLE
                55 -> DENSE_DRIZZLE
                56 -> LIGHT_FREEZING_DRIZZLE
                57 -> DENSE_FREEZING_DRIZZLE
                61 -> SLIGHT_RAIN
                63 -> MODERATE_RAIN
                65 -> HEAVY_RAIN
                66 -> LIGHT_FREEZING_RAIN
                67 -> HEAVY_FREEZING_RAIN
                71 -> SLIGHT_SNOW
                73 -> MODERATE_SNOW
                75 -> HEAVY_SNOW
                77 -> SNOW_GRAINS
                80 -> SLIGHT_RAIN_SHOWERS
                81 -> MODERATE_RAIN_SHOWERS
                82 -> VIOLENT_RAIN_SHOWERS
                85 -> SLIGHT_SNOW_SHOWERS
                86 -> HEAVY_SNOW_SHOWERS
                95 -> SLIGHT_THUNDERSTORM
                96 -> THUNDERSTORM_WITH_SLIGHT_HAIL
                99 -> THUNDERSTORM_WITH_HEAVY_HAIL
                else -> UNKNOWN
            }
        }

    }

}
