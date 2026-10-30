package org.example.kmp.training.di

import dev.zacsweers.metro.createGraph
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel

object IosAppGraph {
    private var graph: AppGraph? = null

    fun initialize() {
        graph = createGraph<AppGraph>()
    }

    fun createWeatherViewModel(): WeatherViewModel {
        return requireNotNull(graph) {
            "IosAppGraph must be initialized before resolving dependencies."
        }.weatherViewModel
    }
}
