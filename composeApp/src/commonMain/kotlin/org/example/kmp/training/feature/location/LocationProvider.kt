package org.example.kmp.training.feature.location

import org.example.kmp.training.feature.location.domain.model.LocationResult

interface LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}