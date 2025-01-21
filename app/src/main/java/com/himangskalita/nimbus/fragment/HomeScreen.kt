package com.himangskalita.nimbus.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.himangskalita.nimbus.R
import com.himangskalita.nimbus.data.Weather
import com.himangskalita.nimbus.databinding.FragmentHomeScreenBinding
import com.himangskalita.nimbus.viewmodel.HomeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeScreen : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding
    private val homeScreenViewModel: HomeScreenViewModel by viewModels()

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            handlePermissionResult(permissions)
        }

    companion object {

        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)

        observeWeatherData()
        setupRefreshListener()
        checkLocationPermissions()

        return binding.root
    }

    private fun observeWeatherData() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeScreenViewModel.weatherData.collect { result ->

                    handleWeatherDataResult(result)
                }
            }
        }
    }

    private fun handleWeatherDataResult(result: Result<Weather>?) {

        when {

            result == null -> binding.fgHsPbLoading.visibility = View.VISIBLE
            result.isSuccess -> updateUI(result.getOrNull())
            result.isFailure -> showErrors(result.exceptionOrNull())
        }
    }

    private fun updateUI(result: Weather?) {

        binding.fgHsPbLoading.visibility = View.GONE

        result?.let {

            val condition = homeScreenViewModel.getWeatherCondition(result.weather_code)

            Log.d("NimbusDebug", result.weather_code.toString())

            binding.apply {

                fgHsTvCityName.text = homeScreenViewModel.getCityName() ?: result.city
                fgHsTvWeatherCode.text = condition.emoji
                fgHsTvCondition.text = condition.description
                fgHsTvTemperature.text = "${it.temperature_2m}°C"
                fgHsTvTemperatureHigh.text = "${it.temperature_2m_max}°C"
                fgHsTvTemperatureLow.text = "${it.temperature_2m_min}°C"
                fgHstvWind.text = "🌬️\n${it.wind_speed_10m}Km/h\nWind"
                fgHstvHumidity.text = "💧\n${it.relative_humidity_2m}%\nHumidity"
                fgHstvRain.text = "🌧️\n${it.rain} mm\nRain"
                root.setBackgroundColor(ContextCompat.getColor(requireContext(), condition.backgroundColor))
            }
        }
    }

    private fun showErrors(error: Throwable?) {

        binding.fgHsPbLoading.visibility = View.GONE

        Toast.makeText(
            requireContext(),
            "Error fetching weather data: ${error?.message}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupRefreshListener() {

        binding.fgHsFabRefresh.setOnClickListener {

            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {

        if (hasLocationPermission()) {

            homeScreenViewModel.fetchLocationAndWeather()
        } else {

            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    private fun hasLocationPermission(): Boolean {

        return LOCATION_PERMISSIONS.all { permission ->

            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun handlePermissionResult(permissions: Map<String, @JvmSuppressWildcards Boolean>) {

        when {

            permissions.all { it.value } -> homeScreenViewModel.fetchLocationAndWeather()
            shouldShowRationale() -> showRationaleDialog()
            else -> showSettingsDialog()
        }
    }

    private fun shouldShowRationale(): Boolean {

        return LOCATION_PERMISSIONS.any {

            shouldShowRequestPermissionRationale(it)
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("Location permission is required to fetch weather data. Would you like to grant it?")
            .setIcon(R.drawable.ic_location)
            .setPositiveButton("Grant") { dialog, _ ->
                checkLocationPermissions()
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showSettingsDialog() {

        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required!")
            .setMessage("This app requires location permission to fetch weather data, give location permission from settings?")
            .setCancelable(false)
            .setIcon(R.drawable.ic_location)
            .setPositiveButton("Settings") { dialog, _ ->

                startActivity((Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)).apply {

                    data = Uri.fromParts("package", requireActivity().packageName, null)
                })

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }
}