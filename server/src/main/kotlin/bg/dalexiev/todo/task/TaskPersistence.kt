package bg.dalexiev.todo.task

import bg.dalexiev.todo.core.db.CrudRepository
import bg.dalexiev.todo.core.db.ExposedCrudRepository
import bg.dalexiev.todo.core.db.dbQuery
import bg.dalexiev.todo.user.Users
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Tasks : LongIdTable() {
    val title = varchar("title", length = 50)
    val description = varchar("description", length = 255)
    val completed = bool("is_completed")
    val createdAt = timestamp("created_at")
    val userId = reference("user_id", refColumn = Users.id, onDelete = ReferenceOption.CASCADE)
}

private val rowToEntity: (ResultRow) -> Task = {
    Task(
        id = it[Tasks.id].value,
        title = it[Tasks.title],
        description = it[Tasks.description],
        completed = it[Tasks.completed],
        createdAt = it[Tasks.createdAt],
        userId = it[Tasks.userId].value
    )
}

class TaskRepository : CrudRepository<Task, Long> by ExposedCrudRepository(
    table = Tasks,
    getId = { it.id },
    entityToRow = { statement, task ->
        statement[Tasks.id] = task.id
        statement[Tasks.title] = task.title
        statement[Tasks.description] = task.description
        statement[Tasks.completed] = task.completed
        statement[Tasks.createdAt] = task.createdAt
        statement[Tasks.userId] = task.userId
    },
    rowToEntity = rowToEntity,
    idPredicate = { Tasks.id eq it }
) {

    suspend fun create(
        userId: Long,
        title: String,
        description: String,
        completed: Boolean = false,
        createdAt: Instant
    ): Task = dbQuery {
        val result = Tasks.insertReturning(returning = Tasks.columns) {
            it[Tasks.title] = title
            it[Tasks.description] = description
            it[Tasks.completed] = completed
            it[Tasks.createdAt] = createdAt
            it[Tasks.userId] = userId
        }.single()

        return@dbQuery rowToEntity(result)
    }

    suspend fun findByUserId(userId: Long): List<Task> = dbQuery {
        Tasks.selectAll()
            .where { Tasks.userId eq userId }
            .map(rowToEntity)
    }

    suspend fun deleteById(id: Long) = dbQuery {
        Tasks.deleteWhere { Tasks.id eq id }
    }
}