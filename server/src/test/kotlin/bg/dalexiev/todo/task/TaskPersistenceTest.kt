package bg.dalexiev.todo.task

import bg.dalexiev.todo.utils.CrudRepositorySpec
import bg.dalexiev.todo.utils.DatabaseOps
import bg.dalexiev.todo.utils.EntityOps
import bg.dalexiev.todo.utils.JdbcTemplate
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlin.random.Random

object TaskEntityOps : EntityOps<Task, Long> {

    override fun createNewEntity(id: Long): Task =
        Task(
            id = id,
            title = "New Task",
            description = "New Task description",
            completed = false,
            createdAt = Clock.System.now(),
            userId = 1L
        )

    override fun generateNewId(): Long = Random.Default.nextLong(1000L, 30000L)
}

class TaskDatabaseOps(private val jdbcTemplate: JdbcTemplate) : DatabaseOps<Task, Long> {

    override fun findEntityById(id: Long): Task? =
        jdbcTemplate.queryForObject(
            "select id, title, description, is_completed, created_at, user_id from tasks where id = ?",
            listOf(id)
        ) {
            Task(
                id = it.getLong("id"),
                title = it.getString("title"),
                description = it.getString("description"),
                completed = it.getBoolean("is_completed"),
                createdAt = it.getTimestamp("created_at").toInstant().toKotlinInstant(),
                userId = it.getLong("user_id")
            )
        }

    override fun insertEntity(entity: Task): Long =
        jdbcTemplate.insert(
            "insert into tasks values(?, ?, ?, ?, ?, ?)",
            listOf(
                entity.id,
                entity.title,
                entity.description,
                entity.completed,
                java.sql.Timestamp.from(entity.createdAt.toJavaInstant()),
                entity.userId
            )
        ) {
            it.getLong("id")
        } ?: throw AssertionError("Could not insert task")
}

class TaskPersistenceTest : CrudRepositorySpec<Task, Long, TaskRepository, TaskDatabaseOps>(
    repo = TaskRepository(),
    entityOps = TaskEntityOps,
    databaseOpsFactory = { TaskDatabaseOps(it) },
    body = {

        val title = "title"
        val description = "description"
        val completed = false
        val createdAt = Clock.System.now()
        val userId = 1L

        val expectedTask = Task(
            id = 1L,
            title = title,
            description = description,
            completed = completed,
            createdAt = createdAt,
            userId
        )
        
        beforeTest {
            insert(
                "insert into users values(?, 'taskTest@test.com', '') on conflict do nothing",
                listOf(userId)
            )    
        }

        context("create") {
            test("inserts task") {
                // when
                val actualTask = create(userId, title, description, completed, createdAt)

                // then
                actualTask shouldBe expectedTask
            }
        }

        context("findByUserId") {
            
            beforeTest { 
                insertEntity(expectedTask)
            }

            test("returns empty list when no tasks with provided id exists") {
                // when
                val actualResult = findByUserId(10L)

                actualResult shouldBe listOf()
            }

            test("returns task") {
                // when
                val actualResult = findByUserId(userId)

                // then
                actualResult shouldBe listOf(expectedTask)
            }
        }
    }
)