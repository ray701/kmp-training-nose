package org.example.kmp.training.feature.Weather.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerializationException
import org.example.kmp.training.core.network.ApiError
import org.example.kmp.training.core.network.ApiResult
import kotlin.coroutines.cancellation.CancellationException

class WeatherApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ): ApiResult<WeatherDto> {
        return try {
            val response = httpClient.get("$baseUrl/weather") {
                parameter("lat", latitude)
                parameter("lon", longitude)
            }

            ApiResult.Success(response.body<WeatherDto>())
        } catch (e: RedirectResponseException) {
            ApiResult.Failure(
                ApiError.HttpError(
                    statusCode = e.response.status.value,
                    message = e.response.bodyAsText(),
                ),
            )
        } catch (e: ClientRequestException) {
            ApiResult.Failure(
                ApiError.HttpError(
                    statusCode = e.response.status.value,
                    message = e.response.bodyAsText(),
                ),
            )
        } catch (e: ServerResponseException) {
            ApiResult.Failure(
                ApiError.HttpError(
                    statusCode = e.response.status.value,
                    message = e.response.bodyAsText(),
                ),
            )
        } catch (e: HttpRequestTimeoutException) {
            ApiResult.Failure(ApiError.Timeout)
        } catch (e: SerializationException) {
            ApiResult.Failure(ApiError.SerializationError)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            ApiResult.Failure(e.toApiError())
        }
    }

}

private fun Throwable.toApiError(): ApiError {
    val message = message.orEmpty().lowercase()

    return when {
        "connection refused" in message ||
                "failed to connect" in message ||
                "connectexception" in message -> {
            ApiError.ServerNotRunning
        }

        "network is unreachable" in message ||
                "no route to host" in message ||
                "unable to resolve host" in message ||
                "nodename nor servname provided" in message ||
                "unknownhost" in message -> {
            ApiError.NetworkUnavailable
        }

        "timeout" in message -> {
            ApiError.Timeout
        }

        else -> {
            ApiError.Unknown(message = this.message)
        }
    }
}