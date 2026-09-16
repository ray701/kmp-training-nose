package org.example.kmp.training.feature.Weather.data.mapper

import org.example.kmp.training.feature.Weather.data.remote.WeatherDto
import org.example.kmp.training.feature.Weather.domain.model.Weather

fun WeatherDto.toDomain(): Weather {
    return Weather(
        location = location,
        temperature = temperature,
        condition = condition,
        humidity = humidity,
    )
}