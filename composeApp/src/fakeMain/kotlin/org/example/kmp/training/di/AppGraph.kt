package org.example.kmp.training.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import org.example.kmp.training.feature.location.LocationProvider
import org.example.kmp.training.feature.location.createLocationProvider
import org.example.kmp.training.feature.location.data.local.AppDatabase
import org.example.kmp.training.feature.location.data.local.RegisteredLocationDao
import org.example.kmp.training.feature.location.data.local.createAppDatabase
import org.example.kmp.training.feature.location.data.repository.RoomRegisteredLocationRepository
import org.example.kmp.training.feature.location.domain.repository.RegisteredLocationRepository
import org.example.kmp.training.feature.weather.data.repository.FakeWeatherRepository
import org.example.kmp.training.feature.weather.domain.repository.WeatherRepository
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel

@DependencyGraph(AppScope::class)
interface AppGraph {
    val weatherViewModel: WeatherViewModel

    @Binds
    val FakeWeatherRepository.bind: WeatherRepository

    @Binds
    val RoomRegisteredLocationRepository.bindRegisteredLocation: RegisteredLocationRepository

    @Provides
    fun provideLocationProvider(): LocationProvider = createLocationProvider()

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase = createAppDatabase()

    @Provides
    fun provideRegisteredLocationDao(database: AppDatabase): RegisteredLocationDao =
        database.registeredLocationDao()
}
