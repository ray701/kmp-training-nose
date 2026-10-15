package org.example.kmp.training.feature.weather.data.mapper

import org.example.kmp.training.feature.weather.data.remote.WeatherDto
import org.example.kmp.training.feature.weather.domain.model.Weather

fun WeatherDto.toDomain(): Weather {
    return Weather(
        location = location,
        temperature = temperature,
        condition = condition,
        humidity = humidity,
    )
}