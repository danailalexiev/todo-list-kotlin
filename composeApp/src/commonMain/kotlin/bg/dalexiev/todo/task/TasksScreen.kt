package bg.dalexiev.todo.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import bg.dalexiev.todo.resources.Res
import bg.dalexiev.todo.resources.tasks_all_done_content_description
import bg.dalexiev.todo.resources.tasks_all_done_label
import bg.dalexiev.todo.resources.tasks_delete_task_button
import bg.dalexiev.todo.resources.tasks_error_content_description
import bg.dalexiev.todo.resources.tasks_error_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskScreen(
    viewModel: TasksViewModel,
    modifier: Modifier = Modifier,
    onNavigateToLoginScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        TasksUiState.NotLoggedIn -> LaunchedEffect(uiState) {
            onNavigateToLoginScreen()
        }

        is TasksUiState.LoggedIn -> TaskScreenContent(
            modifier = modifier,
            uiState = uiState as TasksUiState.LoggedIn,
            onTaskCompleted = {},
            onTaskDeleted = {},
            onRefresh = { viewModel.refresh() }
        )
    }
}

@Composable
private fun TaskScreenContent(
    modifier: Modifier = Modifier,
    uiState: TasksUiState.LoggedIn,
    onTaskCompleted: (Task.PendingTask) -> Unit,
    onTaskDeleted: (Task) -> Unit,
    onRefresh: () -> Unit
) {
    TaskScaffold(modifier = modifier) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(padding),
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh
        ) {
            when (uiState) {
                is TasksUiState.LoggedIn.LoadingFailed -> NoTasks(
                    image = Icons.Filled.Warning,
                    imageContentDescription = Res.string.tasks_error_content_description,
                    text = Res.string.tasks_error_label,
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )

                is TasksUiState.LoggedIn.Loaded -> TaskList(
                    tasks = uiState.tasks,
                    onTaskCompleted = onTaskCompleted,
                    onTaskDeleted = onTaskDeleted,
                    modifier = Modifier.fillMaxSize()
                )

                is TasksUiState.LoggedIn.AllDone -> NoTasks(
                    image = Icons.Filled.Done,
                    imageContentDescription = Res.string.tasks_all_done_content_description,
                    text = Res.string.tasks_all_done_label,
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }

}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onTaskCompleted: (Task.PendingTask) -> Unit,
    onTaskDeleted: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(
            count = tasks.size,
            key = { tasks[it].id },
        ) { index ->
            TaskListItem(
                task = tasks[index],
                onTaskCompleted = onTaskCompleted,
                onTaskDeleted = onTaskDeleted
            )
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    onTaskCompleted: (Task.PendingTask) -> Unit,
    onTaskDeleted: (Task) -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        TaskCompletedCheckbox(
            task = task,
            onCheckedChange = onTaskCompleted
        )

        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(text = task.title.value, style = MaterialTheme.typography.titleSmall.forTask(task))
            Text(text = task.description.value, style = MaterialTheme.typography.labelSmall.forTask(task))
        }

        Spacer(modifier = Modifier.weight(1.0f))

        IconButton(onClick = { onTaskDeleted(task) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.tasks_delete_task_button)
            )
        }
    }
}

@Composable
private fun TaskCompletedCheckbox(
    task: Task,
    onCheckedChange: (Task.PendingTask) -> Unit
) {
    when (task) {
        is Task.CompletedTask -> Checkbox(
            checked = true,
            enabled = false,
            onCheckedChange = {}
        )

        is Task.PendingTask -> Checkbox(
            checked = false,
            enabled = true,
            onCheckedChange = { onCheckedChange(task) }
        )
    }
}

private fun TextStyle.forTask(task: Task) =
    when (task) {
        is Task.CompletedTask -> this.copy(textDecoration = TextDecoration.LineThrough)
        is Task.PendingTask -> this
    }

@Composable
private fun NoTasks(
    image: ImageVector,
    imageContentDescription: StringResource,
    text: StringResource,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = image,
            modifier = Modifier.size(128.dp),
            contentDescription = stringResource(imageContentDescription)
        )
        Text(text = stringResource(text))
    }
}