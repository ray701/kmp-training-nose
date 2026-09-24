package org.example.kmp.training

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.kmp.training.core.network.HttpClientFactory
import org.example.kmp.training.feature.weather.data.remote.WeatherApi
import org.example.kmp.training.feature.weather.data.repository.WeatherRepositoryImpl
import org.example.kmp.training.feature.weather.di.WeatherContainer
import org.example.kmp.training.feature.weather.domain.usecase.GetWeatherUseCase
import org.example.kmp.training.feature.weather.presentation.WeatherScreen
import org.example.kmp.training.feature.weather.presentation.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    val viewModel: WeatherViewModel = viewModel {
                        WeatherContainer.createWeatherViewModel()
                    }

                    LaunchedEffect(Unit) {
                        viewModel.loadWeather(lat = 35.68, lon = 139.76)
                    }

                    WeatherScreen(viewModel = viewModel)
                }
            }
        }
    }
}
