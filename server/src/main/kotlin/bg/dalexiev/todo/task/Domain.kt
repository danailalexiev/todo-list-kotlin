package bg.dalexiev.todo.task

import kotlinx.datetime.Instant

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: Instant,
    val userId: Long
)

fun Task.toResponse() = TaskResponse (
    id = this.id,
    title = this.title,
    description = this.description,
    completed = this.completed,
    createdAt = this.createdAt
)