package bg.dalexiev.todo.auth

import bg.dalexiev.todo.user.EmailAddress
import bg.dalexiev.todo.user.Password
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.authRouting(authService: AuthService) {

    post("/login") {
        val request = call.receive<LoginRequest>()
        when (val result = authService.login(EmailAddress(request.email), Password(request.password))) {
            LoginResult.Failure -> call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
            is LoginResult.Success -> call.respond(HttpStatusCode.OK, LoginResponse(result.token))
        }
    }

}