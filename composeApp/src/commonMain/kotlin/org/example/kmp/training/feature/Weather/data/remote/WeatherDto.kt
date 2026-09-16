package org.example.kmp.training.feature.Weather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("location")
    val location: String,
    @SerialName("temperature")
    val temperature: Double,
    @SerialName("condition")
    val condition: String,
    @SerialName("humidity")
    val humidity: Int,
)
