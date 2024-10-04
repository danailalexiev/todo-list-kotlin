package bg.dalexiev.todo.task

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(onBack: () -> Unit)