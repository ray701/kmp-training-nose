package org.example.kmp.training.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        isLenient = true
                    },
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            expectSuccess = true
        }
    }
}