package org.example.kmp.training.feature.Weather.data.repository

import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.Weather.data.mapper.toDomain
import org.example.kmp.training.feature.Weather.data.remote.WeatherApi
import org.example.kmp.training.feature.Weather.domain.model.Weather
import org.example.kmp.training.feature.Weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherApi: WeatherApi,
) : WeatherRepository {
    override suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ): ApiResult<Weather> {
        return when(val result = weatherApi.getWeather(latitude, longitude)) {
            is ApiResult.Success -> {
                ApiResult.Success(result.data.toDomain())
            }
            is ApiResult.Failure -> {
                result
            }
        }
    }
}