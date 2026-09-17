package org.example.kmp.training.feature.Weather.presentation

import org.example.kmp.training.feature.Weather.domain.model.Weather

sealed interface WeatherUiState {
    data object Loading: WeatherUiState

    data class Success(
        val weather: Weather,
    ) : WeatherUiState

    data class Error(
        val message: String,
    ) : WeatherUiState
}