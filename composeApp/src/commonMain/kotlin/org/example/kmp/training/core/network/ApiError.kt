package org.example.kmp.training.core.network

sealed interface ApiError {
    data object ServerNotRunning : ApiError
    data object Timeout : ApiError
    data object NetworkUnavailable : ApiError
    data object SerializationError : ApiError

    data class HttpError(
        val statusCode: Int,
        val message: String? = null,
    ) : ApiError

    data class Unknown(
        val message: String? = null,
    ) : ApiError
}