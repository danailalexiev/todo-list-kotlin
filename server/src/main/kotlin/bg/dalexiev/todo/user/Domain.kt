package bg.dalexiev.todo.user

import bg.dalexiev.todo.core.validation.isValidEmail
import bg.dalexiev.todo.core.validation.isValidPassword
import bg.dalexiev.todo.core.validation.requireThat
import org.mindrot.jbcrypt.BCrypt

data class UnregisteredUser(val email: EmailAddress, val password: Password)

data class RegisteredUser(val id: Long, val email: EmailAddress, val password: HashedPassword)

@JvmInline
value class EmailAddress(val value: String) {
    init {
        requireThat(value, isValidEmail) { "value is invalid: ${it.joinToString(";")}" }
    }
}

@JvmInline
value class Password(val value: String) {
    init {
        requireThat(value, isValidPassword) { "value is invalid: ${it.joinToString(";")}" }
    }
}

fun Password.hash(): HashedPassword = HashedPassword(BCrypt.hashpw(this.value, BCrypt.gensalt()))

@JvmInline
value class HashedPassword(val value: String) {
    init {
        require(value.isNotBlank()) { "value is blank" }
    }
}

fun HashedPassword.matchesPassword(password: Password): Boolean = BCrypt.checkpw(password.value, this.value)