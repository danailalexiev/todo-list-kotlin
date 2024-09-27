package bg.dalexiev.todo.user

import bg.dalexiev.todo.core.validation.onRequestWithBody
import bg.dalexiev.todo.core.validation.validEmail
import bg.dalexiev.todo.core.validation.validPassword
import io.konform.validation.Validation
import io.ktor.server.plugins.requestvalidation.*

private val validateRegisterUserRequest = Validation {
    RegisterUserRequest::email required { validEmail() }
    RegisterUserRequest::password required { validPassword() }
}

fun RequestValidationConfig.configureUserRequestValidation() {

    onRequestWithBody<RegisterUserRequest> { validateRegisterUserRequest(it) }

}