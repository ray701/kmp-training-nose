package org.example.kmp.training.feature.weather.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.model.LocationResult
import org.example.kmp.training.feature.weather.domain.model.Weather
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository
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

private class FakeLocationProvider(
    private val result: LocationResult,
) : LocationProvider {
    override suspend fun getCurrentLocation(): LocationResult = result
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
            weatherRepository = repository,
            locationProvider = fakeLocationProvider(),
        )

        assertIs<WeatherUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun loadWeather_success_setsSuccessState() = runTest(testDispatcher) {
        val expected = sampleWeather()

        val repository = FakeWeatherRepository(
            result = ApiResult.Success(expected),
        )

        val viewModel = WeatherViewModel(
            weatherRepository = repository,
            locationProvider = fakeLocationProvider(),
        )

        viewModel.loadWeatherByCurrentLocation()

        testScheduler.advanceUntilIdle()

        val state = assertIs<WeatherUiState.Success>(viewModel.uiState.value)
        assertEquals(expected, state.weather)
    }

    @Test
    fun loadWeather_failure_setsErrorState() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(
            result = ApiResult.Failure(ApiError.ServerNotRunning),
        )

        val viewModel = WeatherViewModel(
            weatherRepository = repository,
            locationProvider = fakeLocationProvider(),
        )

        viewModel.loadWeatherByCurrentLocation()

        testScheduler.advanceUntilIdle()

        assertIs<WeatherUiState.Error>(viewModel.uiState.value)
    }

    @Test
    fun loadWeather_setsLoadingBeforeResult() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(
            result = ApiResult.Success(sampleWeather()),
        )

        val viewModel = WeatherViewModel(
            weatherRepository = repository,
            locationProvider = fakeLocationProvider(),
        )

        viewModel.loadWeatherByCurrentLocation()

        // まだコルーチンを進めていないのでLoadingのまま
        assertIs<WeatherUiState.Loading>(viewModel.uiState.value)

        testScheduler.advanceUntilIdle()

        // 完了後はSuccess
        assertIs<WeatherUiState.Success>(viewModel.uiState.value)
    }

    @Test
    fun loadWeather_passesLatitudeAndLongitude() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(
            result = ApiResult.Success(sampleWeather()),
        )

        val viewModel = WeatherViewModel(
            weatherRepository = repository,
            locationProvider = fakeLocationProvider(),
        )

        viewModel.loadWeatherByCurrentLocation()

        testScheduler.advanceUntilIdle()

        assertEquals(35.68, repository.capturedLatitude)
        assertEquals(139.76, repository.capturedLongitude)
    }

    private fun fakeLocationProvider(): LocationProvider {
        return FakeLocationProvider(
            LocationResult.Success(
                Location(
                    latitude = 35.68,
                    longitude = 139.76,
                ),
            ),
        )
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