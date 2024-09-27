package bg.dalexiev.todo

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(appContainer: AppContainer) = ComposeUIViewController { App(appContainer) }