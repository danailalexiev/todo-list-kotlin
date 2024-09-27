package bg.dalexiev.todo.core.db

import org.jetbrains.exposed.exceptions.ExposedSQLException

fun ExposedSQLException.isUniqueConstraintViolation(): Boolean = sqlState == "23505"