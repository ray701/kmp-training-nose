package org.example.kmp.training.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.createLocationProvider
import org.example.kmp.training.feature.weather.data.repository.FakeWeatherRepository
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel

@DependencyGraph(AppScope::class)
interface AppGraph {
    val weatherViewModel: WeatherViewModel

    @Binds
    val FakeWeatherRepository.bind: WeatherRepository

    @Provides
    fun provideLocationProvider(): LocationProvider = createLocationProvider()
}
