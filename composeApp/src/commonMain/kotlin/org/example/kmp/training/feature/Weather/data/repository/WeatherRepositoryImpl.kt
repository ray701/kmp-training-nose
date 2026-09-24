package org.example.kmp.training.feature.weather.data.repository

import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.weather.data.mapper.toDomain
import org.example.kmp.training.feature.weather.data.remote.WeatherApi
import org.example.kmp.training.feature.weather.domain.model.Weather
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherApi: WeatherApi,
) : WeatherRepository {
    override suspend fun getWeather(
        lat: Double,
        lon: Double
    ): ApiResult<Weather> {
        return when(val result = weatherApi.getWeather(lat, lon)) {
            is ApiResult.Success -> {
                ApiResult.Success(result.data.toDomain())
            }
            is ApiResult.Failure -> {
                result
            }
        }
    }
}