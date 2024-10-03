package bg.dalexiev.todo.task

import kotlinx.datetime.Clock

interface TaskService {

    suspend fun getTasks(userId:Long): List<Task>

    suspend fun createTask(userId: Long, title: String, description: String): Task

    suspend fun updateTask(taskId: Long, completed: Boolean): Task?

    suspend fun deleteTask(taskId: Long): Boolean
}

fun taskService(repo: TaskRepository, clock: Clock = Clock.System) = object: TaskService {

    override suspend fun getTasks(userId: Long): List<Task> = repo.findByUserId(userId)

    override suspend fun createTask(userId: Long, title: String, description: String): Task =
        repo.create(
            userId = userId, 
            title = title, 
            description = description,
            createdAt = clock.now()
        )

    override suspend fun updateTask(taskId: Long, completed: Boolean): Task? {
        val task = repo.findById(taskId)
        return task?.let {
            val updatedTask = task.copy(completed = completed)
            repo.save(updatedTask)
        }
    }

    override suspend fun deleteTask(taskId: Long) = repo.deleteById(taskId) > 0

}