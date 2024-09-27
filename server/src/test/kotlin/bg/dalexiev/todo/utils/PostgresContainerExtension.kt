package bg.dalexiev.todo.utils

import bg.dalexiev.todo.user.Users
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerExtension
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer

val postgresContainerExtension = JdbcDatabaseContainerExtension(
    container = PostgreSQLContainer<Nothing>("postgres:12.19-bullseye").apply {
        withDatabaseName("todo_db")
        withUsername("postgres")
        withPassword("password")
    },
    afterTest = { _, _ ->
        transaction {
            Users.deleteAll()
        }
    }
)