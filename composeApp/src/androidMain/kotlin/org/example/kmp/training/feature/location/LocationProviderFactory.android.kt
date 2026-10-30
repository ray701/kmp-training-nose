package org.example.kmp.training.feature.location

actual fun createLocationProvider(): LocationProvider {
    return AndroidLocationProvider(
        context = AndroidAppContext.context,
    )
}