package org.example.kmp.training.feature.weather.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.kmp.training.core.network.ApiResult
import org.example.kmp.training.feature.Weather.data.remote.WeatherApi
import org.example.kmp.training.feature.Weather.data.repository.WeatherRepositoryImpl
import org.example.kmp.training.feature.Weather.domain.model.Weather
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WeatherRepositoryTest {
    @Test
    fun `天気JSONをパースできる`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                      "location": "Test",
                      "temperature": 20.0,
                      "condition": "Sunny",
                      "humidity": 50
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json",
                ),
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        isLenient = true
                    },
                )
            }

            expectSuccess = true
        }

        val weatherApi = WeatherApi(
            httpClient = client,
            baseUrl = "http://localhost:8080",
        )

        val repository = WeatherRepositoryImpl(
            weatherApi = weatherApi,
        )

        val result = repository.getWeather(
            latitude = 35.0,
            longitude = 139.0,
        )

        val success = assertIs<ApiResult.Success<Weather>>(result)

        assertEquals(
            Weather(
                location = "Test",
                temperature = 20.0,
                condition = "Sunny",
                humidity = 50,
            ),
            success.data,
        )
    }
}