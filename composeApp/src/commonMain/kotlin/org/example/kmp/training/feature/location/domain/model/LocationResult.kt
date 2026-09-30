package org.example.kmp.training.feature.location.domain.model

sealed interface LocationResult {
    data class Success(
        val location: Location,
    ) : LocationResult

    data class Failure(
        val error: LocationError,
    ) : LocationResult
}

sealed interface LocationError {
    data object PermissionDenied : LocationError
    data object LocationDisabled : LocationError
    data object Unavailable : LocationError
    data class Unknown(val message: String? = null) : LocationError
}