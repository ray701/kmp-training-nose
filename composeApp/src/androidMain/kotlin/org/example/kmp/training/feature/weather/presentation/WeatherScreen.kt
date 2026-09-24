package org.example.kmp.training.feature.weather.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.kmp.training.feature.weather.domain.model.Weather

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
) {
    // 共通のViewModel / UiStateを使う
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val state = uiState) {
            WeatherUiState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text("読み込み中...")
                }
            }

            is WeatherUiState.Success -> {
                WeatherContent(
                    weather = state.weather,
                    { viewModel.retry() }
                )
            }

            is WeatherUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = { viewModel.retry() }) {
                        Text("再試行")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: Weather, buttonOnClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(weather.location, style = MaterialTheme.typography.headlineMedium)
        Text("${weather.temperature}℃", style = MaterialTheme.typography.displaySmall)
        Text(weather.condition, style = MaterialTheme.typography.titleMedium)
        Text("湿度: ${weather.humidity}%", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = buttonOnClick) {
            Text("再試行")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherContentPreview() {
    MaterialTheme {
        WeatherContent(
            weather = Weather(
                location = "東京",
                temperature = 22.5,
                condition = "晴れ",
                humidity = 45,
            ),
            buttonOnClick = {},
        )
    }
}