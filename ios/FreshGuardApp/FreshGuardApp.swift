import SwiftUI

@main
struct FreshGuardApp: App {
    @StateObject private var store = FreshnessStore()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(store)
        }
    }
}
