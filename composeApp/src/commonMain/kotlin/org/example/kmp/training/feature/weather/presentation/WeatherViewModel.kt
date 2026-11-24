package org.example.kmp.training.feature.weather.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.model.LocationError
import org.example.kmp.training.feature.location.domain.model.LocationResult
import org.example.kmp.training.feature.location.domain.repository.RegisteredLocationRepository
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository

@Inject
class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val registeredLocationRepository: RegisteredLocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var registeredLocationJob: Job? = null

    fun load() {
        loadWeatherByCurrentLocation()
        loadRegisteredLocationWeather()
    }

    fun loadWeatherByCurrentLocation() {
        _uiState.update {
            it.copy(currentLocation = CurrentLocationWeatherUiState.Loading)
        }

        viewModelScope.launch {
            when (val locationResult = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val location = locationResult.location
                    val weatherState = when (
                        val result = weatherRepository.getWeather(
                            location.latitude,
                            location.longitude,
                        )
                    ) {
                        is ApiResult.Success -> CurrentLocationWeatherUiState.Success(
                            location = location,
                            weather = result.data,
                        )
                        is ApiResult.Failure -> CurrentLocationWeatherUiState.Error(
                            result.error.toDisplayMessage(),
                        )
                    }
                    _uiState.update { it.copy(currentLocation = weatherState) }
                }

                is LocationResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            currentLocation = CurrentLocationWeatherUiState.Error(
                                locationResult.error.toDisplayMessage(),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun loadRegisteredLocationWeather() {
        _uiState.update {
            it.copy(registeredLocation = RegisteredLocationWeatherUiState.Loading)
        }

        registeredLocationJob?.cancel()
        registeredLocationJob = viewModelScope.launch {
            try {
                val location = registeredLocationRepository.getRegisteredLocation()
                if (location == null) {
                    _uiState.update {
                        it.copy(
                            registeredLocation = RegisteredLocationWeatherUiState.Unregistered,
                        )
                    }
                    return@launch
                }

                val weatherState = fetchRegisteredLocationWeather(location)
                _uiState.update { it.copy(registeredLocation = weatherState) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        registeredLocation = RegisteredLocationWeatherUiState.Error(
                            "登録地点を読み込めませんでした。",
                        ),
                    )
                }
            }
        }
    }

    fun registerCurrentLocation() {
        val location = (
            _uiState.value.currentLocation as? CurrentLocationWeatherUiState.Success
            )?.location ?: return

        _uiState.update { it.copy(isRegistering = true, registrationError = null) }
        viewModelScope.launch {
            try {
                registeredLocationRepository.saveRegisteredLocation(location)
                _uiState.update { it.copy(isRegistering = false) }
                loadRegisteredLocationWeather()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        isRegistering = false,
                        registrationError = "現在地を登録できませんでした。",
                    )
                }
            }
        }
    }

    fun retryCurrentLocation() = loadWeatherByCurrentLocation()

    fun retryRegisteredLocation() = loadRegisteredLocationWeather()

    private suspend fun fetchRegisteredLocationWeather(
        location: Location,
    ): RegisteredLocationWeatherUiState {
        return when (
            val result = weatherRepository.getWeather(
                location.latitude,
                location.longitude,
            )
        ) {
            is ApiResult.Success -> RegisteredLocationWeatherUiState.Success(
                location = location,
                weather = result.data,
            )
            is ApiResult.Failure -> RegisteredLocationWeatherUiState.Error(
                result.error.toDisplayMessage(),
            )
        }
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