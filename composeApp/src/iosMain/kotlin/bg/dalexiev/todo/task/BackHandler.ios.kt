package bg.dalexiev.todo.task

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(onBack: () -> Unit) {
    // do nothing - no back button on iOS
}