package org.example.kmp.training.feature.weather.presentation

import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.weather.domain.model.Weather

data class WeatherUiState(
    val currentLocation: CurrentLocationWeatherUiState = CurrentLocationWeatherUiState.Loading,
    val registeredLocation: RegisteredLocationWeatherUiState =
        RegisteredLocationWeatherUiState.Loading,
    val isRegistering: Boolean = false,
    val registrationError: String? = null,
)

sealed interface CurrentLocationWeatherUiState {
    data object Loading : CurrentLocationWeatherUiState

    data class Success(
        val location: Location,
        val weather: Weather,
    ) : CurrentLocationWeatherUiState

    data class Error(
        val message: String,
    ) : CurrentLocationWeatherUiState
}

sealed interface RegisteredLocationWeatherUiState {
    data object Loading : RegisteredLocationWeatherUiState

    data object Unregistered : RegisteredLocationWeatherUiState

    data class Success(
        val location: Location,
        val weather: Weather,
    ) : RegisteredLocationWeatherUiState

    data class Error(
        val message: String,
    ) : RegisteredLocationWeatherUiState
}