package org.example.kmp.training.feature.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.model.LocationError
import org.example.kmp.training.feature.location.domain.model.LocationResult
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationProvider {

    // デリゲートを保持しておかないと解放されてしまうため保持する
    private var delegate: CLLocationManagerDelegateProtocol? = null
    private var manager: CLLocationManager? = null

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): LocationResult {
        return suspendCancellableCoroutine { cont ->
            val locationManager = CLLocationManager()

            val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {

                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    when (manager.authorizationStatus) {
                        kCLAuthorizationStatusAuthorizedWhenInUse,
                        kCLAuthorizationStatusAuthorizedAlways -> {
                            manager.requestLocation()
                        }

                        kCLAuthorizationStatusDenied,
                        kCLAuthorizationStatusRestricted -> {
                            if (cont.isActive) {
                                cont.resume(
                                    LocationResult.Failure(LocationError.PermissionDenied),
                                )
                            }
                        }

                        else -> {
                            // 未決定: リクエスト待ち
                        }
                    }
                }

                override fun locationManager(
                    manager: CLLocationManager,
                    didUpdateLocations: List<*>,
                ) {
                    val location = didUpdateLocations.firstOrNull() as? CLLocation
                    if (location != null && cont.isActive) {
                        location.coordinate.useContents {
                            cont.resume(
                                LocationResult.Success(
                                    Location(
                                        latitude = latitude,
                                        longitude = longitude,
                                    ),
                                ),
                            )
                        }
                    }
                }

                override fun locationManager(
                    manager: CLLocationManager,
                    didFailWithError: NSError,
                ) {
                    if (cont.isActive) {
                        cont.resume(
                            LocationResult.Failure(
                                LocationError.Unknown(didFailWithError.localizedDescription),
                            ),
                        )
                    }
                }
            }

            this.manager = locationManager
            this.delegate = locationDelegate

            locationManager.delegate = locationDelegate
            locationManager.requestWhenInUseAuthorization()

            val status = locationManager.authorizationStatus
            if (status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
            ) {
                locationManager.requestLocation()
            }

            cont.invokeOnCancellation {
                locationManager.delegate = null
                this.manager = null
                this.delegate = null
            }
        }
    }
}