package org.example.kmp.training.feature.location

import org.example.kmp.training.feature.location.domain.model.LocationResult

expect class LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}