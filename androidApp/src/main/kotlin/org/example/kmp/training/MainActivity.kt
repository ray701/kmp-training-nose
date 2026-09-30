package org.example.kmp.training

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* 結果はViewModel側の再取得で反映 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )

        setContent {
            MaterialTheme {
                Surface {
                    val viewModel: WeatherViewModel = viewModel {
                        WeatherContainer.createWeatherViewModel()
                    }

                    LaunchedEffect(Unit) {
                        viewModel.loadWeatherByCurrentLocation()
                    }

                    WeatherScreen(viewModel = viewModel)
                }
            }
        }
    }
}
