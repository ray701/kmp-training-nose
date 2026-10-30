import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosAppGraph.shared.initialize()
    }

    var body: some Scene {
        WindowGroup {
            WeatherView()
        }
    }
}