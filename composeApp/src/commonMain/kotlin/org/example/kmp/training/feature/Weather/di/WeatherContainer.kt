package org.example.kmp.training.feature.weather.di

import org.example.kmp.training.core.network.HttpClientFactory
import org.example.kmp.training.feature.weather.data.remote.WeatherApi
import org.example.kmp.training.feature.weather.data.repository.WeatherRepositoryImpl
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository
import org.example.kmp.training.feature.weather.domain.usecase.GetWeatherUseCase
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel
import org.example.kmp.training.getMockServerBaseUrl

object WeatherContainer {
    private val httpClient = HttpClientFactory.create()

    private val weatherApi = WeatherApi(
        httpClient = httpClient,
        baseUrl = getMockServerBaseUrl(),
    )

    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl(
        weatherApi = weatherApi,
    )

    private val getWeatherUseCase = GetWeatherUseCase(
        weatherRepository = weatherRepository,
    )

    fun createWeatherViewModel(): WeatherViewModel {
        return WeatherViewModel(
            getWeatherUseCase = getWeatherUseCase,
        )
    }
}