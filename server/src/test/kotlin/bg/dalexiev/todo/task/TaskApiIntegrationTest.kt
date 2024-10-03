package bg.dalexiev.todo.task

import bg.dalexiev.todo.utils.ApiIntegrationTestSpec
import bg.dalexiev.todo.utils.JdbcTemplate
import bg.dalexiev.todo.utils.appScenario
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.mindrot.jbcrypt.BCrypt

class TaskApiIntegrationTest : ApiIntegrationTestSpec({

    val authenticatedUserId = 100L

    val title = "Test title"
    val description = "Test description"
    val completed = false
    val createdAt = Clock.System.now()

    feature("GET /tasks") {
        appScenario("returns 401 Unauthorized if no token provided") {
            val response = get("/tasks")

            response shouldHaveStatus HttpStatusCode.Unauthorized
        }

        appScenario("returns 200 OK", authenticatedUserId = authenticatedUserId) {
            insertUser(authenticatedUserId)

            val taskId = insertTask(title, description, completed, createdAt, authenticatedUserId)

            val response = get("/tasks")

            response shouldHaveStatus HttpStatusCode.OK
            response.body<TasksResponse>() shouldBe listOf(
                TaskResponse(
                    id = taskId,
                    title = title,
                    description = description,
                    completed = completed,
                    createdAt = createdAt
                )
            )
        }
    }

    feature("POST /tasks") {
        appScenario("returns 401 Unauthorized if no token provided") {
            val response = post("/tasks") {
                contentType(ContentType.Application.Json)

                setBody(CreateTaskRequest("title", "description"))
            }

            response shouldHaveStatus HttpStatusCode.Unauthorized
        }

        appScenario("returns 201 Created", authenticatedUserId = authenticatedUserId) {
            insertUser(authenticatedUserId)

            val response = post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(CreateTaskRequest(title, description))
            }

            response shouldHaveStatus HttpStatusCode.Created
            response.body<TaskResponse>().shouldBeEqualToIgnoringFields(
                other = TaskResponse(
                    id = response.headers[HttpHeaders.Location]?.substring(1)?.toLong()
                        ?: throw AssertionError("Missing location header in response"),
                    title = title,
                    description = description,
                    completed = false,
                    createdAt = createdAt
                ),
                property = TaskResponse::createdAt
            )
        }
    }

    feature("PATCH /tasks/{taskId}") {
        appScenario("returns 401 Unauthorized if no token provided") {
            val response = patch("/tasks/1") {
                contentType(ContentType.Application.Json)
                setBody(UpdateTaskRequest(completed = true))
            }

            response shouldHaveStatus HttpStatusCode.Unauthorized
        }

        appScenario("returns 404 Not Found if no task with {taskId} found", authenticatedUserId = authenticatedUserId) {
            insertUser(authenticatedUserId)

            val response = patch("/tasks/1") {
                contentType(ContentType.Application.Json)
                setBody(UpdateTaskRequest(completed = true))
            }

            response shouldHaveStatus HttpStatusCode.NotFound
        }

        appScenario("returns 200 OK", authenticatedUserId = authenticatedUserId) {
            insertUser(authenticatedUserId)
            val taskId = insertTask(title, description, completed, createdAt, userId = authenticatedUserId)

            val response = patch("/tasks/$taskId") {
                contentType(ContentType.Application.Json)
                setBody(UpdateTaskRequest(completed = true))
            }

            response shouldHaveStatus HttpStatusCode.OK
            response.body<TaskResponse>() shouldBe TaskResponse(
                id = taskId,
                title = title,
                description = description,
                completed = true,
                createdAt = createdAt
            )
        }
    }

    feature("DELETE /tasks/{taskId}") {
        appScenario("returns 401 Unauthorized if no token provided") {
            val response = delete("/tasks/1") {
                contentType(ContentType.Application.Json)
            }

            response shouldHaveStatus HttpStatusCode.Unauthorized
        }

        appScenario("returns 404 Not Found if no task with {taskId} found", authenticatedUserId = authenticatedUserId) {
            insertUser(authenticatedUserId)

            val response = delete("/tasks/1") {
                contentType(ContentType.Application.Json)
            }

            response shouldHaveStatus HttpStatusCode.NotFound
        }

        appScenario("returns 204 No Content", authenticatedUserId = authenticatedUserId) {
            insertUser(authenticatedUserId)
            val taskId = insertTask(title, description, completed, createdAt, userId = authenticatedUserId)

            val response = delete("/tasks/$taskId") {
                contentType(ContentType.Application.Json)
            }

            response shouldHaveStatus HttpStatusCode.NoContent

        }
    }
})

private fun JdbcTemplate.insertUser(id: Long) {
    insert(
        "insert into users(id, email, password) values (?, ?, ?)",
        listOf(id, "test@test.com", BCrypt.hashpw("Secret!23", BCrypt.gensalt()))
    )
}

private fun JdbcTemplate.insertTask(
    title: String,
    description: String,
    completed: Boolean,
    createdAt: Instant,
    userId: Long
): Long =
    insert(
        "insert into tasks(title, description, is_completed, created_at, user_id) values (?, ?, ?, ?, ?)",
        listOf(
            title,
            description,
            completed,
            java.sql.Timestamp.from(createdAt.toJavaInstant()),
            userId
        )
    ) {
        it.getLong("id")
    } ?: throw AssertionError("Could not insert task")