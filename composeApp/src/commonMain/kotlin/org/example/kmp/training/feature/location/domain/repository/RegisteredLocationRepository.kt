package org.example.kmp.training.feature.location.domain.repository

import org.example.kmp.training.feature.location.domain.model.Location

interface RegisteredLocationRepository {
    suspend fun getRegisteredLocation(): Location?

    suspend fun saveRegisteredLocation(location: Location)
}
