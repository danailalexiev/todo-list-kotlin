import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    
    private let appContainer: AppContainer
    
    init(appContainer: AppContainer) {
        self.appContainer = appContainer
    }
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(appContainer: appContainer)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView(appContainer: AppContainer())
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}



