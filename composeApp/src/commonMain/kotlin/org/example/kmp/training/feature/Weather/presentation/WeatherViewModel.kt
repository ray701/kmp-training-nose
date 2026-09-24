package org.example.kmp.training.feature.weather.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.weather.domain.usecase.GetWeatherUseCase

class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(
        lat: Double = DEFAULT_LATITUDE,
        lon: Double = DEFAULT_LONGITUDE,
    ) {
        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            val newState = when (val result = getWeatherUseCase(lat = lat, lon = lon,)) {
                is ApiResult.Success -> WeatherUiState.Success(result.data)
                is ApiResult.Failure -> WeatherUiState.Error(result.error.toDisplayMessage())
            }

            _uiState.value = newState
            logState(_uiState.value)
        }
    }

    fun retry() {
        loadWeather()
    }

    private fun logState(state: WeatherUiState) {
        println("WeatherUiState=$state")  // Android では Log.d でも可
    }
    companion object {
        private const val DEFAULT_LATITUDE = 35.68
        private const val DEFAULT_LONGITUDE = 139.76
    }
}

private fun ApiError.toDisplayMessage(): String {
    return when (this) {
        ApiError.ServerNotRunning -> "サーバーに接続できませんでした。"
        ApiError.Timeout -> "通信がタイムアウトしました。"
        ApiError.NetworkUnavailable -> "ネットワークに接続できません。"
        ApiError.SerializationError -> "データの読み込みに失敗しました。"
        is ApiError.HttpError -> "エラーが発生しました。（$statusCode）"
        is ApiError.Unknown -> "不明なエラーが発生しました。"
    }
}
