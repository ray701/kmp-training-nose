package org.example.kmp.training.feature.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.model.LocationError
import org.example.kmp.training.feature.location.domain.model.LocationResult
import kotlin.coroutines.resume

actual class LocationProvider(
    private val context: Context,
) {
    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    actual suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.Failure(LocationError.PermissionDenied)
        }

        return try {
            val current = requestCurrentLocation()
            val location = current ?: requestLastLocation()

            if (location != null) {
                LocationResult.Success(
                    Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                    ),
                )
            } else {
                LocationResult.Failure(LocationError.Unavailable)
            }
        } catch (e: SecurityException) {
            LocationResult.Failure(LocationError.PermissionDenied)
        } catch (e: Throwable) {
            LocationResult.Failure(LocationError.Unknown(e.message))
        }
    }

    private suspend fun requestCurrentLocation(): android.location.Location? {
        val cancellationTokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token,
            ).addOnSuccessListener { location ->
                cont.resume(location)
            }.addOnFailureListener {
                cont.resume(null)
            }

            cont.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    private suspend fun requestLastLocation(): android.location.Location? {
        return suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }.addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }
}