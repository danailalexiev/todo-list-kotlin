package bg.dalexiev.todo.plugins

import bg.dalexiev.todo.auth.configureAuthRequestValidation
import bg.dalexiev.todo.user.configureUserRequestValidation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureValidation() {
    install(RequestValidation) {
        configureUserRequestValidation()
        configureAuthRequestValidation()
    }
    
    install(StatusPages) {
        exception<RequestValidationException> { call, cause -> 
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }
}