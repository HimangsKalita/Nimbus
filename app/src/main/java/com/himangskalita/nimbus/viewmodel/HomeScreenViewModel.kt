package com.himangskalita.nimbus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.himangskalita.nimbus.data.Weather
import com.himangskalita.nimbus.di.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(

    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _weatherData = MutableStateFlow<Result<Weather>?>(null)
    val weatherData: StateFlow<Result<Weather>?>
        get() = _weatherData

    fun fetchWeatherData(queryParams: Map<String, String>) {

        viewModelScope.launch(Dispatchers.IO) {

            _weatherData.value = weatherRepository.fetchWeatherDataFromApi(queryParams)
        }
    }
}