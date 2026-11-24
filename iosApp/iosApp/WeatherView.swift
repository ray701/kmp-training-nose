//
// Created by rei.nose on 2026/09/25.
//

import SwiftUI
import ComposeApp

struct WeatherView: View {

    @StateObject private var viewModel = WeatherViewModelWrapper()

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                GroupBox("現在地") {
                    currentLocationContent
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                GroupBox("登録地点") {
                    registeredLocationContent
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .padding()
        }
        .onAppear {
            viewModel.load()
        }
    }

    @ViewBuilder
    private var currentLocationContent: some View {
        let state = viewModel.uiState.currentLocation
        VStack(alignment: .leading, spacing: 12) {
            if state is CurrentLocationWeatherUiStateLoading {
                ProgressView("読み込み中...")
            } else if let success = state as? CurrentLocationWeatherUiStateSuccess {
                weatherContent(
                    location: success.location,
                    weather: success.weather
                )
                Button(viewModel.uiState.isRegistering ? "登録中..." : "この位置を登録") {
                    viewModel.registerCurrentLocation()
                }
                .disabled(viewModel.uiState.isRegistering)
                if let message = viewModel.uiState.registrationError {
                    Text(message)
                        .foregroundStyle(.red)
                }
            } else if let error = state as? CurrentLocationWeatherUiStateError {
                Text(error.message)
                    .foregroundStyle(.red)
                Button("再試行") {
                    viewModel.retryCurrentLocation()
                }
            }
        }
    }

    @ViewBuilder
    private var registeredLocationContent: some View {
        let state = viewModel.uiState.registeredLocation
        VStack(alignment: .leading, spacing: 12) {
            if state is RegisteredLocationWeatherUiStateLoading {
                ProgressView("読み込み中...")
            } else if state is RegisteredLocationWeatherUiStateUnregistered {
                Text("登録地点は未登録です。")
            } else if let success = state as? RegisteredLocationWeatherUiStateSuccess {
                weatherContent(
                    location: success.location,
                    weather: success.weather
                )
            } else if let error = state as? RegisteredLocationWeatherUiStateError {
                Text(error.message)
                    .foregroundStyle(.red)
                Button("再試行") {
                    viewModel.retryRegisteredLocation()
                }
            }
        }
    }

    private func weatherContent(
        location: Location,
        weather: Weather
    ) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(weather.location)
                .font(.title2)
            Text("\(weather.temperature)℃")
                .font(.largeTitle)
            Text(weather.condition)
                .font(.title3)
            Text("湿度: \(weather.humidity)%")
            Text("緯度: \(location.latitude), 経度: \(location.longitude)")
                .font(.footnote)
        }
    }
}

#Preview {
    WeatherView()
}
