package bg.dalexiev.todo.task

import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline

sealed interface Task {

    val id: Long
    val title: Title
    val description: Description
    val createdAt: CreationTime

    data class PendingTask(
        override val id: Long,
        override val title: Title,
        override val description: Description,
        override val createdAt: CreationTime
    ) : Task

    data class CompletedTask(
        override val id: Long,
        override val title: Title,
        override val description: Description,
        override val createdAt: CreationTime
    ) : Task

}

fun TaskResponse.toTask(): Task =
    if (completed) {
        Task.CompletedTask(
            id = id,
            title = Title(title),
            description = Description(description),
            createdAt = CreationTime(createdAt)
        )
    } else {
        Task.PendingTask(
            id = id,
            title = Title(title),
            description = Description(description),
            createdAt = CreationTime(createdAt)
        )
    }

@JvmInline
value class Title(val value: String)

@JvmInline
value class Description(val value: String)

@JvmInline
value class CreationTime(val value: Instant)