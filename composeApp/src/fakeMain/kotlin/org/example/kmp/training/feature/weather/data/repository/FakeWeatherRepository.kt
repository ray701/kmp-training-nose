package org.example.kmp.training.feature.weather.data.repository

import dev.zacsweers.metro.Inject
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.weather.domain.model.Weather
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository

@Inject
class FakeWeatherRepository : WeatherRepository {
    override suspend fun getWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<Weather> = ApiResult.Success(
        Weather(
            location = "Fake City",
            temperature = 20.0,
            condition = "Sunny",
            humidity = 50,
        ),
    )
}
