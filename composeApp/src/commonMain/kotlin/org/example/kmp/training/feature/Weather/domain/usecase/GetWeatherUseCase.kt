package org.example.kmp.training.feature.Weather.domain.usecase

import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.Weather.domain.model.Weather
import org.example.kmp.training.feature.Weather.domain.repository.WeatherRepository

class GetWeatherUseCase(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(
        lat: Double,
        lon: Double,
    ): ApiResult<Weather> {
        return weatherRepository.getWeather(
            lat = lat,
            lon = lon,
        )
    }
}