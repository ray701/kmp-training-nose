package org.example.kmp.training.feature.weather.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.Weather.domain.model.Weather
import org.example.kmp.training.feature.Weather.domain.repository.WeatherRepository
import org.example.kmp.training.feature.Weather.domain.usecase.GetWeatherUseCase
import org.example.kmp.training.feature.Weather.presentation.WeatherUiState
import org.example.kmp.training.feature.Weather.presentation.WeatherViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// commonTest 内に、固定データを返す Fake を定義する
private class FakeWeatherRepository(
    private val result: ApiResult<Weather>,
) : WeatherRepository {
    var capturedLatitude: Double? = null
        private set

    var capturedLongitude: Double? = null
        private set

    override suspend fun getWeather(
        lat: Double,
        lon: Double
    ): ApiResult<Weather> {
        capturedLatitude = lat
        capturedLongitude = lon
        return result
    }
}
class WeatherViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        val repository = FakeWeatherRepository(
            result = ApiResult.Success(sampleWeather()),
        )

        val viewModel = WeatherViewModel(
            getWeatherUseCase = GetWeatherUseCase(repository),
        )

        assertIs<WeatherUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun loadWeather_success_setsSuccessState() = runTest {
        val expected = sampleWeather()

        val repository = FakeWeatherRepository(
            result = ApiResult.Success(expected),
        )

        val viewModel = WeatherViewModel(
            getWeatherUseCase = GetWeatherUseCase(repository),
        )

        viewModel.loadWeather(lat = 35.68, lon = 139.76)

        testScheduler.advanceUntilIdle()

        val state = assertIs<WeatherUiState.Success>(viewModel.uiState.value)
        assertEquals(expected, state.weather)
    }

    @Test
    fun loadWeather_failure_setsErrorState() = runTest {
        val repository = FakeWeatherRepository(
            result = ApiResult.Failure(ApiError.ServerNotRunning),
        )

        val viewModel = WeatherViewModel(
            getWeatherUseCase = GetWeatherUseCase(repository),
        )

        viewModel.loadWeather(lat = 35.68, lon = 139.76)

        testScheduler.advanceUntilIdle()

        assertIs<WeatherUiState.Error>(viewModel.uiState.value)
    }

    @Test
    fun loadWeather_setsLoadingBeforeResult() = runTest {
        val repository = FakeWeatherRepository(
            result = ApiResult.Success(sampleWeather()),
        )

        val viewModel = WeatherViewModel(
            getWeatherUseCase = GetWeatherUseCase(repository),
        )

        viewModel.loadWeather(lat = 35.68, lon = 139.76)

        // まだコルーチンを進めていないのでLoadingのまま
        assertIs<WeatherUiState.Loading>(viewModel.uiState.value)

        testScheduler.advanceUntilIdle()

        // 完了後はSuccess
        assertIs<WeatherUiState.Success>(viewModel.uiState.value)
    }

    @Test
    fun loadWeather_passesLatitudeAndLongitude() = runTest {
        val repository = FakeWeatherRepository(
            result = ApiResult.Success(sampleWeather()),
        )

        val viewModel = WeatherViewModel(
            getWeatherUseCase = GetWeatherUseCase(repository),
        )

        viewModel.loadWeather(lat = 35.68, lon = 139.76)

        testScheduler.advanceUntilIdle()

        assertEquals(35.68, repository.capturedLatitude)
        assertEquals(139.76, repository.capturedLongitude)
    }
    private fun sampleWeather(): Weather {
        return Weather(
            location = "Tokyo area (Lat: 35.68, Lon: 139.76)",
            temperature = 20.0,
            condition = "Sunny",
            humidity = 50,
        )
    }
}