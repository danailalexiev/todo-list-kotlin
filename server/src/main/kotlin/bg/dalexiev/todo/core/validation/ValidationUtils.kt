package bg.dalexiev.todo.core.validation

import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern
import io.ktor.server.plugins.requestvalidation.*

typealias KonformValidationResult<T> = io.konform.validation.ValidationResult<T>

fun ValidationBuilder<String>.notBlank() = addConstraint("must not be blank") { it.isNotBlank() }

fun ValidationBuilder<String>.validEmail() {
    notBlank()
    maxLength(50)
    pattern("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$") hint "must be a valid email address"
}

fun ValidationBuilder<String>.validPassword() {
    minLength(8)
    maxLength(50)
    pattern("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\\\$!%*?&])[A-Za-z\\d@\\\$!%*?&]{8,}\$") hint "must contain at least one lowercase letter, one uppercase letter, a special char and a digit"
}

val isValidPassword = Validation { validPassword() }

val isValidEmail = Validation { validEmail() }

inline fun <reified T : Any> RequestValidationConfig.onRequestWithBody(crossinline block: (T) -> KonformValidationResult<T>) {
    validate<T> {
        when (val result = block(it)) {
            is Invalid -> ValidationResult.Invalid(result.errors.map { error -> "${error.dataPath}: ${error.message}" })
            is Valid -> ValidationResult.Valid
        }
    }
}

inline fun <reified V : Any> requireThat(
    value: V,
    validation: Validation<V>,
    message: (List<String>) -> String
) {
    val result = validation.validate(value)
    if (result is Invalid) {
        throw IllegalStateException(message(result.errors.map { it.message }))
    }
}