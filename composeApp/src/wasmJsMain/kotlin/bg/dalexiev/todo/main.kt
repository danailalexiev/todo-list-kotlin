package bg.dalexiev.todo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appContainer = AppContainer()
    ComposeViewport(document.body!!) {
        App(appContainer)
    }
}