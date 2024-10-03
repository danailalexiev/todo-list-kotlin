package bg.dalexiev.todo.task

import bg.dalexiev.todo.core.validation.notBlank
import bg.dalexiev.todo.core.validation.onRequestWithBody
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.ktor.server.plugins.requestvalidation.*

fun RequestValidationConfig.configureTaskRequestValidation() {

    onRequestWithBody<CreateTaskRequest> {
        Validation {
            CreateTaskRequest::title required {
                notBlank()
                maxLength(50)
            }

            CreateTaskRequest::description required {
                notBlank()
                maxLength(255)
            }
        }.validate(it)
    }

    onRequestWithBody<UpdateTaskRequest> {
        Validation {
            UpdateTaskRequest::completed required {}
        }.validate(it)
    }

}