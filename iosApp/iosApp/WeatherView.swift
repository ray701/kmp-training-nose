//
// Created by rei.nose on 2026/09/25.
//

import SwiftUI
import ComposeApp

struct WeatherView: View {

    @StateObject private var viewModel = WeatherViewModelWrapper()

    var body: some View {
        VStack(spacing: 16) {
            content
        }
        .padding()
        .onAppear {
            viewModel.load()
        }
    }

    @ViewBuilder
    private var content: some View {
        // sealed interface を as? で分岐
        if viewModel.uiState is WeatherUiStateLoading {
            ProgressView()
            Text("読み込み中...")
        } else if let success = viewModel.uiState as? WeatherUiStateSuccess {
            let weather = success.weather
            Text(weather.location)
                .font(.title)
            Text("\(weather.temperature)℃")
                .font(.largeTitle)
            Text(weather.condition)
                .font(.title3)
            Text("湿度: \(weather.humidity)%")
            Button("再試行") {
                viewModel.retry()
            }
        } else if let error = viewModel.uiState as? WeatherUiStateError {
            Text(error.message)
                .multilineTextAlignment(.center)
            Button("再試行") {
                viewModel.retry()
            }
        }
    }
}

#Preview {
    WeatherView()
}
