package bg.dalexiev.todo.user

import bg.dalexiev.todo.core.db.CrudRepository
import bg.dalexiev.todo.core.db.ExposedCrudRepository
import bg.dalexiev.todo.core.db.dbQuery
import bg.dalexiev.todo.core.db.isUniqueConstraintViolation
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll

object Users : LongIdTable() {
    val email = varchar("email", length = 50).uniqueIndex("idx_email")
    val password = varchar("password", length = 255)
}

private val rowToEntity: (ResultRow) -> RegisteredUser = {
    RegisteredUser(
        id = it[Users.id].value,
        email = EmailAddress(it[Users.email]),
        password = HashedPassword(it[Users.password])
    )
}

class UserRepository : CrudRepository<RegisteredUser, Long> by ExposedCrudRepository(
    table = Users,
    getId = { it.id },
    entityToRow = { statement, registeredUser ->
            statement[Users.id] = registeredUser.id
            statement[Users.email] = registeredUser.email.value
            statement[Users.password] = registeredUser.password.value
    },
    rowToEntity = rowToEntity,
    idPredicate = { Users.id eq it }
) {

    suspend fun create(email: String, password: String): RegisteredUser? = dbQuery {
        try {
            Users.insertReturning(returning = listOf(Users.id, Users.email, Users.password)) {
                it[Users.email] = email
                it[Users.password] = password
            }.singleOrNull()
        } catch (e: ExposedSQLException) {
            if (e.isUniqueConstraintViolation()) {
                return@dbQuery null
            }
            throw e
        }
    }?.let(rowToEntity)

    suspend fun findByEmail(email: String): RegisteredUser? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .singleOrNull()
    }?.let(rowToEntity)
}