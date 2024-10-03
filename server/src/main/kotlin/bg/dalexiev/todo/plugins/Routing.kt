package bg.dalexiev.todo.plugins

import bg.dalexiev.todo.auth.authRouting
import bg.dalexiev.todo.di.Dependencies
import bg.dalexiev.todo.task.tasksRouting
import bg.dalexiev.todo.user.usersRouting
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(dependencies: Dependencies) {
    routing {
        usersRouting(dependencies.userService)
        authRouting(dependencies.authService)
        tasksRouting(dependencies.taskService)
    }
}
