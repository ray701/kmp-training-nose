package org.example.kmp.training.feature.location

import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.model.LocationResult

actual class LocationProvider {
    actual suspend fun getCurrentLocation(): LocationResult {
        // Desktopは位置情報非対応: 固定値（東京）を返す
        return LocationResult.Success(
            Location(
                latitude = 35.68,
                longitude = 139.76,
            ),
        )
    }
}