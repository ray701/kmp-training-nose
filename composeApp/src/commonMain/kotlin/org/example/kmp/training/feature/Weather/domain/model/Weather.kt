package org.example.kmp.training.feature.Weather.domain.model

data class Weather(
    val location: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
)
