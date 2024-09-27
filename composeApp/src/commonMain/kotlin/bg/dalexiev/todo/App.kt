package bg.dalexiev.todo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bg.dalexiev.todo.login.LoginScreen
import bg.dalexiev.todo.login.LoginViewModel
import bg.dalexiev.todo.resources.Res
import bg.dalexiev.todo.resources.login_title
import bg.dalexiev.todo.resources.tasks_title
import bg.dalexiev.todo.task.TaskScreen
import bg.dalexiev.todo.task.TasksViewModel
import org.jetbrains.compose.resources.StringResource

enum class ToDoScreen(val route: String, val title: StringResource) {
    LOGIN("login", Res.string.login_title),
    TASKS("tasks", Res.string.tasks_title)
}

@Composable
fun App(
    appContainer: AppContainer,
    navHostController: NavHostController = rememberNavController()
) {
    MaterialTheme {
        NavHost(
            navController = navHostController,
            startDestination = ToDoScreen.TASKS.route
        ) {
            composable(route = ToDoScreen.TASKS.route) {
                TaskScreen(
                    viewModel = viewModel {
                        TasksViewModel(
                            appContainer.taskRepository,
                            appContainer.tokenStore
                        )
                    },
                    onNavigateToLoginScreen = { navHostController.navigate(ToDoScreen.LOGIN.route) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = ToDoScreen.LOGIN.route) {
                LoginScreen(
                    viewModel = viewModel {
                        LoginViewModel(
                            appContainer.userRepository,
                            appContainer.tokenStore
                        )
                    },
                    onLoginCompleted = { navHostController.popBackStack() },
                    onExitApp = { },
                    modifier = Modifier.fillMaxSize()
                        .padding(32.dp)
                )
            }
        }
    }
}