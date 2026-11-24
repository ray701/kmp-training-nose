package org.example.kmp.training.feature.weather.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.weather.domain.model.Weather

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        WeatherSection(title = "現在地") {
            when (val state = uiState.currentLocation) {
                CurrentLocationWeatherUiState.Loading -> LoadingContent()
                is CurrentLocationWeatherUiState.Success -> {
                    WeatherContent(
                        location = state.location,
                        weather = state.weather,
                    )
                    Button(
                        onClick = viewModel::registerCurrentLocation,
                        enabled = !uiState.isRegistering,
                    ) {
                        Text(if (uiState.isRegistering) "登録中..." else "この位置を登録")
                    }
                    uiState.registrationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
                is CurrentLocationWeatherUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::retryCurrentLocation) {
                        Text("再試行")
                    }
                }
            }
        }

        WeatherSection(title = "登録地点") {
            when (val state = uiState.registeredLocation) {
                RegisteredLocationWeatherUiState.Loading -> LoadingContent()
                RegisteredLocationWeatherUiState.Unregistered -> {
                    Text("登録地点は未登録です。")
                }
                is RegisteredLocationWeatherUiState.Success -> WeatherContent(
                    location = state.location,
                    weather = state.weather,
                )
                is RegisteredLocationWeatherUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::retryRegisteredLocation) {
                        Text("再試行")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            content()
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text("読み込み中...")
    }
}

@Composable
private fun WeatherContent(
    location: Location,
    weather: Weather,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(weather.location, style = MaterialTheme.typography.headlineMedium)
        Text("${weather.temperature}℃", style = MaterialTheme.typography.displaySmall)
        Text(weather.condition, style = MaterialTheme.typography.titleMedium)
        Text("湿度: ${weather.humidity}%", style = MaterialTheme.typography.bodyLarge)
        Text("緯度: ${location.latitude}, 経度: ${location.longitude}")
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherContentPreview() {
    MaterialTheme {
        WeatherContent(
            location = Location(
                latitude = 35.68,
                longitude = 139.76,
            ),
            weather = Weather(
                location = "東京",
                temperature = 22.5,
                condition = "晴れ",
                humidity = 45,
            ),
        )
    }
}