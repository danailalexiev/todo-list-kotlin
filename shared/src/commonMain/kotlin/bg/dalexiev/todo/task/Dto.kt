package bg.dalexiev.todo.task

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias TasksResponse = List<TaskResponse>

@Serializable
data class TaskResponse(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("completed") val completed: Boolean,
    @SerialName("createdAt") val createdAt: Instant
)

@Serializable
data class CreateTaskRequest(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String
)

@Serializable
data class UpdateTaskRequest(
    @SerialName("completed") val completed: Boolean
)

