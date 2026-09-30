package org.example.kmp.training.feature.location

import org.example.kmp.training.feature.location.createLocationProvider
actual fun createLocationProvider(): LocationProvider {
    return LocationProvider(
        context = AndroidAppContext.context,
    )
}