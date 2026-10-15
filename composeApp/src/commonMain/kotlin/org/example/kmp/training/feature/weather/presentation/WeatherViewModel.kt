package org.example.kmp.training.feature.weather.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.domain.model.LocationError
import org.example.kmp.training.feature.location.domain.model.LocationResult
import org.example.kmp.training.feature.weather.domain.usecase.GetWeatherUseCase

class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeatherByCurrentLocation() {
        updateState(WeatherUiState.Loading)

        viewModelScope.launch {
            when (val locationResult = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val loc = locationResult.location
                    println("WeatherTraining: location=lat ${loc.latitude}, lon ${loc.longitude}")
                    fetchWeather(loc.latitude, loc.longitude)
                }

                is LocationResult.Failure -> {
                    updateState(
                        WeatherUiState.Error(locationResult.error.toDisplayMessage()),
                    )
                }
            }
        }
    }

    fun retry() {
        loadWeatherByCurrentLocation()
    }

    private suspend fun fetchWeather(lat: Double, lon: Double) {
        val state = when (val result = getWeatherUseCase(lat, lon)) {
            is ApiResult.Success -> WeatherUiState.Success(result.data)
            is ApiResult.Failure -> WeatherUiState.Error(result.error.toDisplayMessage())
        }
        updateState(state)
    }

    private fun updateState(state: WeatherUiState) {
        _uiState.value = state
        println("WeatherUiState=$state")
    }
}

private fun ApiError.toDisplayMessage(): String = when (this) {
    ApiError.ServerNotRunning -> "サーバーに接続できませんでした。"
    ApiError.Timeout -> "通信がタイムアウトしました。"
    ApiError.NetworkUnavailable -> "ネットワークに接続できません。"
    ApiError.SerializationError -> "データの読み込みに失敗しました。"
    is ApiError.HttpError -> "エラーが発生しました。（$statusCode）"
    is ApiError.Unknown -> "不明なエラーが発生しました。"
}

private fun LocationError.toDisplayMessage(): String = when (this) {
    LocationError.PermissionDenied -> "位置情報の権限が許可されていません。"
    LocationError.LocationDisabled -> "位置情報がオフになっています。"
    LocationError.Unavailable -> "現在地を取得できませんでした。"
    is LocationError.Unknown -> "位置情報の取得に失敗しました。"
}