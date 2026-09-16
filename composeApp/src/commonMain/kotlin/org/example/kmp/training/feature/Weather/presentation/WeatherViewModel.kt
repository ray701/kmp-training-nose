package org.example.kmp.training.feature.Weather.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.Weather.domain.usecase.GetWeatherUseCase

class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
    ) {
        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            when (
                val result = getWeatherUseCase(
                    latitude = latitude,
                    longitude = longitude,
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.value = WeatherUiState.Success(result.data)
                    logState(_uiState.value)
                }

                is ApiResult.Failure -> {
                    _uiState.value = WeatherUiState.Error(
                        message = result.error.toDisplayMessage(),
                    )
                    logState(_uiState.value)
                }
            }
        }
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
        ApiError.ServerNotRunning -> "サーバーが起動していない可能性があります。"
        ApiError.Timeout -> "通信がタイムアウトしました。"
        ApiError.NetworkUnavailable -> "ネットワークに接続できません。"
        ApiError.SerializationError -> "レスポンスの解析に失敗しました。"
        is ApiError.HttpError -> "HTTPエラーが発生しました。status=$statusCode"
        is ApiError.Unknown -> "不明なエラーが発生しました。${message.orEmpty()}"
    }
}