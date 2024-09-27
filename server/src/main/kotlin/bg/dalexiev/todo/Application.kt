package bg.dalexiev.todo

import bg.dalexiev.todo.di.Dependencies
import bg.dalexiev.todo.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val environment = Environment()
    val dependencies = Dependencies(environment)
    embeddedServer(Netty, port = environment.http.port, host = environment.http.host, module = { module(environment, dependencies) })
        .start(wait = true)
}

fun Application.module(environment: Environment, dependencies: Dependencies) {
    configureSerialization()
    configureDatabases(dependencies.dataSource)
    configureSecurity(environment.auth, dependencies.authService)
    configureValidation()
    configureRouting(dependencies)
}
