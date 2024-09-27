package bg.dalexiev.todo.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersRouting(service: UserService) {
    post("/users") {
        val registerUserRequest = call.receive<RegisterUserRequest>()
        when (val result = service.registerUser(registerUserRequest.toUnregisteredUser())) {
            is RegistrationResult.DuplicateEmail -> call.respond(
                HttpStatusCode.BadRequest,
                "Email address already exists"
            )

            is RegistrationResult.Success -> call.respond(
                HttpStatusCode.Created,
                result.user.toResponse()
            )
        }
    }
}