package org.example.kmp.training.di

import dev.zacsweers.metro.createGraph
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel

object AndroidAppGraph {
    private var graph: AppGraph? = null

    fun initialize() {
        graph = createGraph<AppGraph>()
    }

    fun createWeatherViewModel(): WeatherViewModel {
        return requireNotNull(graph) {
            "AndroidAppGraph must be initialized before resolving dependencies."
        }.weatherViewModel
    }
}
