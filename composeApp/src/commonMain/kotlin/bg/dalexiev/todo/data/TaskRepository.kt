package bg.dalexiev.todo.data

import bg.dalexiev.todo.task.CreateTaskRequest
import bg.dalexiev.todo.task.Task
import bg.dalexiev.todo.task.UpdateTaskRequest
import bg.dalexiev.todo.util.Either
import bg.dalexiev.todo.util.catch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

interface TaskRepository {

    suspend fun getTasks(): Either<Throwable, List<Task>>

    suspend fun createTask(title: String, description: String): Either<Throwable, Task>

    suspend fun markAsDone(task: Task): Either<Throwable, Task>

    suspend fun deleteTask(task: Task): Either<Throwable, Unit>

}

fun taskRepository(httpClient: HttpClient, baseUrl: String): TaskRepository =
    object : TaskRepository {

        override suspend fun getTasks(): Either<Throwable, List<Task>> = catch {
            httpClient.get("$baseUrl/tasks").body()
        }

        override suspend fun createTask(
            title: String,
            description: String
        ): Either<Throwable, Task> = catch {
            httpClient.post("$baseUrl/tasks") {
                setBody(CreateTaskRequest(title, description))
            }.body()
        }

        override suspend fun markAsDone(task: Task): Either<Throwable, Task> = catch {
            httpClient.patch("$baseUrl/tasks/${task.id}") {
                setBody(UpdateTaskRequest(completed = true))
            }.body()
        }

        override suspend fun deleteTask(task: Task): Either<Throwable, Unit> = catch {
            httpClient.delete("$baseUrl/tasks/${task.id}")
        }
    }

fun fakeTaskRepository(): TaskRepository = object : TaskRepository {

    private val tasks = mutableListOf(
        Task(
            id = 1L,
            title = "Test task",
            description = "Fake task to test UI",
            completed = false,
            createdAt = Clock.System.now()
        )
    )

    override suspend fun getTasks(): Either<Throwable, List<Task>> {
        delay(1000L)
        return Either.Success(tasks.sortedByDescending { it.createdAt })
    }

    override suspend fun createTask(title: String, description: String): Either<Throwable, Task> {
        val task = Task(
            id = tasks.maxOf { it.id } + 1,
            title = title,
            description = description,
            completed = false,
            createdAt = Clock.System.now()
        ).also { tasks.add(it) }

        return Either.Success(task)
    }

    override suspend fun markAsDone(task: Task): Either<Throwable, Task> {
        val index = tasks.indexOf(task)
        if (index > -1) {
            tasks.removeAt(index)
            val doneTask = task.copy(completed = true)
            tasks.add(index, doneTask)
            return Either.Success(doneTask)
        }

        return Either.Failure(IllegalArgumentException("Task not found"))
    }

    override suspend fun deleteTask(task: Task): Either<Throwable, Unit> =
        if (tasks.remove(task)) {
            Either.Success(Unit)
        } else {
            Either.Failure(IllegalArgumentException("Task not found"))
        }

}