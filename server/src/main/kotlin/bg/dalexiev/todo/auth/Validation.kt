package bg.dalexiev.todo.auth

import bg.dalexiev.todo.core.validation.onRequestWithBody
import bg.dalexiev.todo.core.validation.validEmail
import bg.dalexiev.todo.core.validation.validPassword
import io.konform.validation.Validation
import io.ktor.server.plugins.requestvalidation.*

private val validateLoginRequest = Validation {
    LoginRequest::email required { validEmail() }
    LoginRequest::password required { validPassword() }
}

fun RequestValidationConfig.configureAuthRequestValidation() {
    onRequestWithBody<LoginRequest> { validateLoginRequest(it) }
}