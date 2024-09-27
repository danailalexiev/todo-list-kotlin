package bg.dalexiev.todo.plugins

import bg.dalexiev.todo.auth.authRouting
import bg.dalexiev.todo.di.Dependencies
import bg.dalexiev.todo.user.usersRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(dependencies: Dependencies) {
    routing { 
        usersRouting(dependencies.userService)
        authRouting(dependencies.authService)
    }
}
