@file:OptIn(ExperimentalMaterial3Api::class)

package bg.dalexiev.todo.task

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bg.dalexiev.todo.resources.Res
import bg.dalexiev.todo.resources.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TaskAppBar(
                title = stringResource(Res.string.app_name),
            )
        },
        modifier = modifier,
        content = content
    )
}

@Composable
private fun TaskAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
    )
}