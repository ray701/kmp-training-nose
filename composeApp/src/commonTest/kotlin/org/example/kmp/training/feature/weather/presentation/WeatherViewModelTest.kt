package org.example.kmp.training.feature.weather.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.model.LocationError
import org.example.kmp.training.feature.location.domain.model.LocationResult
import org.example.kmp.training.feature.location.domain.repository.RegisteredLocationRepository
import org.example.kmp.training.feature.weather.domain.model.Weather
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// commonTest 内に、固定データを返す Fake を定義する
private class RecordingWeatherRepository(
    private val resultFor: suspend (Double, Double) -> ApiResult<Weather> = { _, _ ->
        ApiResult.Success(sampleWeather())
    },
) : WeatherRepository {
    val requestedCoordinates = mutableListOf<Pair<Double, Double>>()

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<Weather> {
        requestedCoordinates += lat to lon
        return resultFor(lat, lon)
    }
}

private class FakeLocationProvider(
    private val result: LocationResult,
) : LocationProvider {
    override suspend fun getCurrentLocation(): LocationResult = result
}

private class FakeRegisteredLocationRepository(
    initialLocation: Location? = null,
) : RegisteredLocationRepository {
    var location: Location? = initialLocation
        private set

    override suspend fun getRegisteredLocation(): Location? = location

    override suspend fun saveRegisteredLocation(location: Location) {
        this.location = location
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun initialState_hasBothSectionsLoading() {
        val viewModel = createViewModel()

        assertIs<CurrentLocationWeatherUiState.Loading>(
            viewModel.uiState.value.currentLocation,
        )
        assertIs<RegisteredLocationWeatherUiState.Loading>(
            viewModel.uiState.value.registeredLocation,
        )
    }

    @Test
    fun load_withoutRegistration_showsCurrentWeatherAndUnregistered() =
        runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.load()
        testScheduler.advanceUntilIdle()

        assertIs<CurrentLocationWeatherUiState.Success>(
            viewModel.uiState.value.currentLocation,
        )
        assertIs<RegisteredLocationWeatherUiState.Unregistered>(
            viewModel.uiState.value.registeredLocation,
        )
    }

    @Test
    fun load_usesPersistedCoordinatesForRegisteredWeather() = runTest(testDispatcher) {
        val weatherRepository = RecordingWeatherRepository()
        val registeredLocation = Location(34.69, 135.50)
        val viewModel = createViewModel(
            weatherRepository = weatherRepository,
            registeredRepository = FakeRegisteredLocationRepository(registeredLocation),
        )

        viewModel.load()
        testScheduler.advanceUntilIdle()

        assertEquals(true, weatherRepository.requestedCoordinates.contains(34.69 to 135.50))
        val state = assertIs<RegisteredLocationWeatherUiState.Success>(
            viewModel.uiState.value.registeredLocation,
        )
        assertEquals(registeredLocation, state.location)
    }

    @Test
    fun registerCurrentLocation_savesCoordinatesAndRefreshesRegisteredWeather() =
        runTest(testDispatcher) {
        val registeredRepository = FakeRegisteredLocationRepository()
        val viewModel = createViewModel(
            registeredRepository = registeredRepository,
        )

        viewModel.loadWeatherByCurrentLocation()
        testScheduler.advanceUntilIdle()
        viewModel.registerCurrentLocation()
        testScheduler.advanceUntilIdle()

        assertEquals(CURRENT_LOCATION, registeredRepository.location)
        assertIs<RegisteredLocationWeatherUiState.Success>(
            viewModel.uiState.value.registeredLocation,
        )
    }

    @Test
    fun registerCurrentLocation_ignoresStaleRegisteredWeatherResponse() =
        runTest(testDispatcher) {
        val viewModel = createViewModel(
            weatherRepository = RecordingWeatherRepository { lat, _ ->
                if (lat == 34.69) delay(10_000)
                ApiResult.Success(sampleWeather())
            },
            registeredRepository = FakeRegisteredLocationRepository(
                Location(34.69, 135.50),
            ),
        )

        viewModel.load()
        testScheduler.runCurrent()
        viewModel.registerCurrentLocation()
        testScheduler.advanceUntilIdle()

        val state = assertIs<RegisteredLocationWeatherUiState.Success>(
            viewModel.uiState.value.registeredLocation,
        )
        assertEquals(CURRENT_LOCATION, state.location)
    }

    @Test
    fun currentLocationFailure_doesNotHideRegisteredWeather() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            locationProvider = FakeLocationProvider(
                LocationResult.Failure(LocationError.PermissionDenied),
            ),
            registeredRepository = FakeRegisteredLocationRepository(
                Location(34.69, 135.50),
            ),
        )

        viewModel.load()
        testScheduler.advanceUntilIdle()

        assertIs<CurrentLocationWeatherUiState.Error>(
            viewModel.uiState.value.currentLocation,
        )
        assertIs<RegisteredLocationWeatherUiState.Success>(
            viewModel.uiState.value.registeredLocation,
        )
    }

    @Test
    fun registeredWeatherFailure_doesNotHideCurrentWeather() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            weatherRepository = RecordingWeatherRepository { lat, _ ->
                if (lat == 34.69) {
                    ApiResult.Failure(ApiError.NetworkUnavailable)
                } else {
                    ApiResult.Success(sampleWeather())
                }
            },
            registeredRepository = FakeRegisteredLocationRepository(
                Location(34.69, 135.50),
            ),
        )

        viewModel.load()
        testScheduler.advanceUntilIdle()

        assertIs<CurrentLocationWeatherUiState.Success>(
            viewModel.uiState.value.currentLocation,
        )
        assertIs<RegisteredLocationWeatherUiState.Error>(
            viewModel.uiState.value.registeredLocation,
        )
    }

    private fun createViewModel(
        weatherRepository: WeatherRepository = RecordingWeatherRepository(),
        locationProvider: LocationProvider = FakeLocationProvider(
            LocationResult.Success(CURRENT_LOCATION),
        ),
        registeredRepository: RegisteredLocationRepository =
            FakeRegisteredLocationRepository(),
    ) = WeatherViewModel(
        weatherRepository = weatherRepository,
        locationProvider = locationProvider,
        registeredLocationRepository = registeredRepository,
    )

    private companion object {
        val CURRENT_LOCATION = Location(
            latitude = 35.68,
            longitude = 139.76,
        )
    }
}

private fun sampleWeather(): Weather = Weather(
    location = "Tokyo area (Lat: 35.68, Lon: 139.76)",
    temperature = 20.0,
    condition = "Sunny",
    humidity = 50,
)