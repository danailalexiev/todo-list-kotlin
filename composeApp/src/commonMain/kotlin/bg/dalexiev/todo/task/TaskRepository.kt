package bg.dalexiev.todo.task

import bg.dalexiev.todo.task.CreateTaskRequest
import bg.dalexiev.todo.task.TaskResponse
import bg.dalexiev.todo.task.TasksResponse
import bg.dalexiev.todo.task.UpdateTaskRequest
import bg.dalexiev.todo.util.Either
import bg.dalexiev.todo.util.catch
import bg.dalexiev.todo.util.mapCatching
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

    suspend fun createTask(title: String, description: String): Either<Throwable, Task.PendingTask>

    suspend fun markAsDone(task: Task.PendingTask): Either<Throwable, Task.CompletedTask>

    suspend fun deleteTask(task: Task): Either<Throwable, Unit>

}

fun taskRepository(httpClient: HttpClient, baseUrl: String): TaskRepository =
    object : TaskRepository {

        override suspend fun getTasks(): Either<Throwable, List<Task>> = catch {
            httpClient.get("$baseUrl/tasks")
                .body<TasksResponse>()
                .map { it.toTask() }
        }

        override suspend fun createTask(
            title: String,
            description: String
        ): Either<Throwable, Task.PendingTask> = catch {
            httpClient.post("$baseUrl/tasks") {
                setBody(CreateTaskRequest(title, description))
            }.body<TaskResponse>().toTask()
        }.mapCatching {
            when (it) {
                is Task.CompletedTask -> throw IllegalStateException("Expected pending task, but was completed")
                is Task.PendingTask -> it
            }
        }

        override suspend fun markAsDone(task: Task.PendingTask): Either<Throwable, Task.CompletedTask> =
            catch {
                httpClient.patch("$baseUrl/tasks/${task.id}") {
                    setBody(UpdateTaskRequest(completed = true))
                }.body<TaskResponse>().toTask()
            }.mapCatching {
                when (it) {
                    is Task.CompletedTask -> it
                    is Task.PendingTask -> throw IllegalStateException("Expected completed task, but was pending")
                }
            }

        override suspend fun deleteTask(task: Task): Either<Throwable, Unit> = catch {
            httpClient.delete("$baseUrl/tasks/${task.id}")
        }
    }

fun fakeTaskRepository(): TaskRepository = object : TaskRepository {

    private val tasks = mutableListOf<Task>(
        Task.PendingTask(
            id = 1L,
            title = Title("Test task"),
            description = Description("Fake task to test UI"),
            createdAt = CreationTime(Clock.System.now())
        )
    )

    override suspend fun getTasks(): Either<Throwable, List<Task>> {
        delay(1000L)
        return Either.Success(tasks.sortedByDescending { it.createdAt.value })
    }

    override suspend fun createTask(
        title: String,
        description: String
    ): Either<Throwable, Task.PendingTask> {
        val task = Task.PendingTask(
            id = tasks.maxOf { it.id } + 1,
            title = Title(title),
            description = Description(description),
            createdAt = CreationTime(Clock.System.now())
        ).also { tasks.add(it) }

        return Either.Success(task)
    }

    override suspend fun markAsDone(task: Task.PendingTask): Either<Throwable, Task.CompletedTask> {
        val index = tasks.indexOf(task)
        if (index > -1) {
            tasks.removeAt(index)
            val completedTask = Task.CompletedTask(
                id = task.id,
                title = task.title,
                description = task.description,
                createdAt = task.createdAt
            )
            tasks.add(index, completedTask)
            return Either.Success(completedTask)
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