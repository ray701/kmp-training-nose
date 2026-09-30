package org.example.kmp.training.feature.weather.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.kmp.training.feature.weather.di.WeatherContainer

class WeatherViewModelObserver {
    private val viewModel: WeatherViewModel = WeatherContainer.createWeatherViewModel()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    // Swiftから呼ぶ: uiStateをcollectして変化を通知
    fun startObserving(onChange: (WeatherUiState) -> Unit) {
        job = scope.launch {
            viewModel.uiState.collect { state ->
                onChange(state)
            }
        }
    }

    fun loadWeatherByCurrentLocation() {
        viewModel.loadWeatherByCurrentLocation()
    }

    fun retry() {
        viewModel.retry()
    }

    // Swiftのdeinitで呼ぶ: 購読解除
    fun stopObserving() {
        job?.cancel()
        job = null
    }
}