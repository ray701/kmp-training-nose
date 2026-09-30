package org.example.kmp.training.feature.location

import org.example.kmp.training.feature.location.LocationProvider

actual fun createLocationProvider(): LocationProvider {
    return LocationProvider()
}