package org.example.kmp.training.feature.location

import org.example.kmp.training.feature.location.domain.LocationProvider

actual fun createLocationProvider(): LocationProvider {
    return LocationProvider()
}
