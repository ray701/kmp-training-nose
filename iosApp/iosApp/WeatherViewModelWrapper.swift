//
// Created by rei.nose on 2026/09/25.
//

import Foundation
import ComposeApp

@MainActor
final class WeatherViewModelWrapper: ObservableObject {

    @Published var uiState: WeatherUiState

    private let observer = WeatherViewModelObserver()

    init() {
        // 初期状態は Loading
        self.uiState = WeatherUiStateLoading()

        observer.startObserving { [weak self] state in
            self?.uiState = state
        }
    }

    func load() {
        observer.loadWeatherByCurrentLocation()
    }

    func retry() {
        observer.retry()
    }

    deinit {
        observer.stopObserving()
    }
}
