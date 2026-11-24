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
        self.uiState = observer.initialState()

        observer.startObserving { [weak self] state in
            self?.uiState = state
        }
    }

    func load() {
        observer.load()
    }

    func registerCurrentLocation() {
        observer.registerCurrentLocation()
    }

    func retryCurrentLocation() {
        observer.retryCurrentLocation()
    }

    func retryRegisteredLocation() {
        observer.retryRegisteredLocation()
    }

    deinit {
        observer.stopObserving()
    }
}
