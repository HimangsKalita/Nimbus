package com.himangskalita.nimbus.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.himangskalita.nimbus.R
import com.himangskalita.nimbus.databinding.FragmentHomeScreenBinding
import com.himangskalita.nimbus.viewmodel.HomeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class HomeScreen : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding
    private lateinit var homeScreenViewModel: HomeScreenViewModel
    private lateinit var queryParams: MutableMap<String, String>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var cityName: String? = null

    companion object {
        private const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)

        homeScreenViewModel = ViewModelProvider(this)[HomeScreenViewModel::class.java]

        checkAndRequestLocationPermission()

        queryParams = mutableMapOf(
            "latitude" to "0.00",
            "longitude" to "0.00",
            "current" to "temperature_2m,relative_humidity_2m,rain,weather_code,wind_speed_10m",
            "daily" to "temperature_2m_max,temperature_2m_min"
        )

        val weatherCodeToCondition = mapOf(
            0 to Pair("Clear sky", "☀️"),
            1 to Pair("Mainly clear", "🌤️"),
            2 to Pair("Partly cloudy", "⛅"),
            3 to Pair("Overcast", "☁️"),
            45 to Pair("Fog", "🌫️"),
            48 to Pair("Depositing rime fog", "🌫️"),
            51 to Pair("Light drizzle", "🌦️"),
            53 to Pair("Moderate drizzle", "🌧️"),
            55 to Pair("Dense drizzle", "🌧️"),
            56 to Pair("Light freezing drizzle", "❄️"),
            57 to Pair("Dense freezing drizzle", "❄️"),
            61 to Pair("Slight rain", "🌦️"),
            63 to Pair("Moderate rain", "🌧️"),
            65 to Pair("Heavy rain", "🌧️"),
            66 to Pair("Light freezing rain", "❄️"),
            67 to Pair("Heavy freezing rain", "❄️"),
            71 to Pair("Slight snow", "🌨️"),
            73 to Pair("Moderate snow", "🌨️"),
            75 to Pair("Heavy snow", "🌨️"),
            77 to Pair("Snow grains", "🌨️"),
            80 to Pair("Slight rain showers", "🌦️"),
            81 to Pair("Moderate rain showers", "🌧️"),
            82 to Pair("Violent rain showers", "🌧️"),
            85 to Pair("Slight snow showers", "❄️"),
            86 to Pair("Heavy snow showers", "❄️"),
            95 to Pair("Slight thunderstorm", "⛈️"),
            96 to Pair("Thunderstorm with slight hail", "⛈️"),
            99 to Pair("Thunderstorm with heavy hail", "⛈️")
        )

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeScreenViewModel.weatherData.collect { result ->

                    when {

                        result == null -> {

                            binding.fgHsPbLoading.visibility = View.VISIBLE
                        }

                        result.isSuccess -> {

                            val weather = result.getOrNull()
                            binding.apply {

                                val (condition, emoji) = weatherCodeToCondition[weather?.weather_code]
                                    ?: Pair("Unknown", "❓")

                                setBackgroundColor(weather?.weather_code)

                                fgHsPbLoading.visibility = View.GONE
                                fgHsTvCityName.text = cityName
                                fgHsTvWeatherCode.text = emoji
                                fgHsTvTemperature.text = weather?.temperature_2m.toString() + "°C"
                                fgHsTvCondition.text = condition
                                fgHsTvTemperatureHigh.text =
                                    "H: " + weather?.temperature_2m_max.toString() + "°C |"
                                fgHsTvTemperatureLow.text =
                                    "L: " + weather?.temperature_2m_min.toString() + "°C"
                                fgHstvWind.text =
                                    "🌬️\n" + weather?.wind_speed_10m.toString() + "Km/h\nWind"
                                fgHstvHumidity.text =
                                    "💧\n" + weather?.relative_humidity_2m.toString() + "%\nHumidity"
                                fgHstvRain.text = "🌧️\n" + weather?.rain.toString() + " mm\nRain"
                            }
                        }

                        result.isFailure -> {

                            Toast.makeText(
                                requireContext(),
                                "Failed to fetch weather data: ${result.exceptionOrNull()?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        binding.fgHsFabRefresh.setOnClickListener {

            checkAndRequestLocationPermission()
        }

        return binding.root
    }

    private fun setBackgroundColor(weatherCode: Int?) {

        if (weatherCode == null) {

            binding.root.setBackgroundResource(R.color.white)
        } else {

            when(weatherCode) {

                0 -> binding.root.setBackgroundResource(R.color.lightBlue)

                // Mainly clear, partly cloudy, overcast
                in 1..3 -> binding.root.setBackgroundResource(R.color.grey)

                // Fog and depositing rime fog
                in 45..48 -> binding.root.setBackgroundResource(R.color.darkGrey)

                // Drizzle (Light, moderate, and dense)
                in 51..55 -> binding.root.setBackgroundResource(R.color.lightBlue)

                // Freezing drizzle
                in 56..57 -> binding.root.setBackgroundResource(R.color.lightBlue)

                // Rain (Slight, moderate, and heavy)
                in 61..65 -> binding.root.setBackgroundResource(R.color.blue)

                // Freezing rain
                in 66..67 -> binding.root.setBackgroundResource(R.color.blue)

                // Snow fall (Slight, moderate, and heavy)
                in 71..75 -> binding.root.setBackgroundResource(R.color.white)

                // Snow grains
                77 -> binding.root.setBackgroundResource(R.color.white)

                // Rain showers (Slight, moderate, and violent)
                in 80..82 -> binding.root.setBackgroundResource(R.color.blue)

                // Snow showers (Slight and heavy)
                in 85..86 -> binding.root.setBackgroundResource(R.color.white)

                // Thunderstorm (Slight or moderate)
                95, 96 -> binding.root.setBackgroundResource(R.color.darkGrey)

                // Thunderstorm with slight and heavy hail
                99 -> binding.root.setBackgroundResource(R.color.darkGrey)

                else -> binding.root.setBackgroundResource(R.color.white)
            }
        }
    }

    private fun checkAndRequestLocationPermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            onLocationPermissionGranted()
        } else {

            val requestLocationPermissionContract =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

                    when {

                        !permissions.containsValue(false) -> {

                            onLocationPermissionGranted()

                        }

                        else -> {

                            if (!shouldShowRequestPermissionRationale(FINE_LOCATION) && !shouldShowRequestPermissionRationale(
                                    COARSE_LOCATION
                                )
                            ) {

                                showSettingsDialog()
                            } else {

                                Toast.makeText(
                                    requireContext(),
                                    "Location permission denied, app won't fetch weather details",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

            if (shouldShowRequestPermissionRationale(FINE_LOCATION) || shouldShowRequestPermissionRationale(
                    COARSE_LOCATION
                )
            ) {

                showRationaleDialog()
            } else {

                requestLocationPermissionContract.launch(arrayOf(FINE_LOCATION, COARSE_LOCATION))
            }
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("Location permission is required to fetch weather data. Would you like to grant it?")
            .setIcon(R.drawable.ic_location)
            .setPositiveButton("Grant") { _, _ ->
                requestPermission()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                Toast.makeText(
                    requireContext(),
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(FINE_LOCATION, COARSE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun showSettingsDialog() {

        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("This app requires location permission to fetch weather data, give location permission from settings?")
            .setCancelable(false)
            .setIcon(R.drawable.ic_location)
            .setPositiveButton("Settings") { dialog, _ ->

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                val uri = Uri.fromParts("package", requireActivity().packageName, null)

                intent.setData(uri)

                startActivity(intent)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun onLocationPermissionGranted() {
        Toast.makeText(requireContext(), "Location permissions granted", Toast.LENGTH_SHORT).show()
        getGpsCoordinates()
    }

    @SuppressLint("MissingPermission")
    private fun getGpsCoordinates() {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {

                val latitude = location.latitude
                val longitude = location.longitude

                queryParams["latitude"] = latitude.toString()
                queryParams["longitude"] = longitude.toString()

                homeScreenViewModel.fetchWeatherData(queryParams)

                fun getCityName(): String? {
                    return try {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (addresses != null) {
                            addresses[0].locality
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                cityName = getCityName()
            }

            locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                TimeUnit.SECONDS.toMillis(60)
            ).apply {

                setGranularity(Granularity.GRANULARITY_FINE)
                setWaitForAccurateLocation(true)
            }.build()

        }

    }

}