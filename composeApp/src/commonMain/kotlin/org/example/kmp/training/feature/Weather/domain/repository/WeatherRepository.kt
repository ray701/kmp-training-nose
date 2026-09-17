package org.example.kmp.training.feature.Weather.domain.repository

import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.Weather.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<Weather>
}