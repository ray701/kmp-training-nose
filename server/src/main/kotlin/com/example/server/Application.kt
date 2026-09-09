package com.example.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val location: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int
)

@Serializable
data class ForecastResponse(
    val location: String,
    val daily: List<DailyForecast>
)

@Serializable
data class DailyForecast(
    val date: String,
    val low: Double,
    val high: Double,
    val condition: String
)

/**
 * 研修用: 緯度・経度からおおよその地域ラベルを返す（厳密なジオコーディングではない）。
 * シミュレータの位置を変えたとき、天気の location が変わることを確認しやすくする。
 */
internal fun resolveRegionLabel(lat: Double, lon: Double): String {
    return when {
        lat in 35.0..36.2 && lon in 139.2..140.2 -> "Tokyo area"
        lat in 34.2..35.2 && lon in 134.8..135.8 -> "Osaka area"
        lat in 42.8..43.4 && lon in 140.8..141.6 -> "Sapporo area"
        lat in 33.4..34.0 && lon in 130.2..130.8 -> "Fukuoka area"
        lat in 26.0..28.5 && lon in 127.5..128.5 -> "Okinawa area"
        else -> "Other area"
    }
}

fun main() {
    println("Starting server on http://0.0.0.0:8080")
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/") {
                call.respondText("Weather Mock Server is running!")
            }
            get("/weather") {
                val lat = call.parameters["lat"]?.toDoubleOrNull() ?: 0.0
                val lon = call.parameters["lon"]?.toDoubleOrNull() ?: 0.0
                val region = resolveRegionLabel(lat, lon)
                call.respond(
                    WeatherResponse(
                        location = "$region (Lat: $lat, Lon: $lon)",
                        temperature = 25.5,
                        condition = "Sunny",
                        humidity = 60
                    )
                )
            }
            get("/forecast") {
                val lat = call.parameters["lat"]?.toDoubleOrNull() ?: 0.0
                val lon = call.parameters["lon"]?.toDoubleOrNull() ?: 0.0
                val region = resolveRegionLabel(lat, lon)
                call.respond(
                    ForecastResponse(
                        location = region,
                        daily = listOf(
                            DailyForecast("2023-10-27", 15.0, 22.0, "Cloudy"),
                            DailyForecast("2023-10-28", 14.0, 25.0, "Sunny"),
                            DailyForecast("2023-10-29", 16.0, 20.0, "Rainy")
                        )
                    )
                )
            }
        }
    }.start(wait = true)
}
