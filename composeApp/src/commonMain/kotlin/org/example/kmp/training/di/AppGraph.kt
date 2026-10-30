package org.example.kmp.training.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Scope
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import org.example.kmp.training.core.network.HttpClientFactory
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.createLocationProvider
import org.example.kmp.training.feature.weather.data.remote.WeatherApi
import org.example.kmp.training.feature.weather.data.repository.RealWeatherRepository
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel
import org.example.kmp.training.getMockServerBaseUrl

@Scope
annotation class AppScope

@DependencyGraph(AppScope::class)
interface AppGraph {
    val weatherViewModel: WeatherViewModel

    @Binds
    val RealWeatherRepository.bind: WeatherRepository

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = HttpClientFactory.create()

    @Provides
    fun provideWeatherApi(httpClient: HttpClient): WeatherApi = WeatherApi(
        httpClient = httpClient,
        baseUrl = getMockServerBaseUrl(),
    )

    @Provides
    fun provideLocationProvider(): LocationProvider = createLocationProvider()
}
