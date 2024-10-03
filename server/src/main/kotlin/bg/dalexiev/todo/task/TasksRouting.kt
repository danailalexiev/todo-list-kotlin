package bg.dalexiev.todo.task

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

fun Routing.tasksRouting(taskService: TaskService) = authenticate("jwt-auth") {
    get("/tasks") {
        val userId = call.receiveAuthenticatedUserId()
        val tasks = taskService.getTasks(userId).map { it.toResponse() }
        call.respond(HttpStatusCode.OK, tasks)
    }

    post("/tasks") {
        val userId = call.receiveAuthenticatedUserId()
        val request = call.receive<CreateTaskRequest>()
        val task = taskService.createTask(userId, request.title, request.description).toResponse()
        call.response.header(HttpHeaders.Location, "/${task.id}")
        call.respond(HttpStatusCode.Created, task)
    }

    patch("/tasks/{taskId}") {
        val taskId = call.getTaskId()
        val request = call.receive<UpdateTaskRequest>()
        val task = taskService.updateTask(taskId, request.completed)?.toResponse()
        task?.let { call.respond(HttpStatusCode.OK, task) } ?: call.respond(HttpStatusCode.NotFound)
    }

    delete("/tasks/{taskId}") {
        val taskId = call.getTaskId()
        if (taskService.deleteTask(taskId)) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

private fun RoutingCall.receiveAuthenticatedUserId(): Long =
    principal<JWTPrincipal>()?.payload?.getClaim("id")?.asLong()
        ?: throw IllegalStateException("Required JWT missing")

private fun RoutingCall.getTaskId(): Long =
    parameters["taskId"]?.toLong()
        ?: throw IllegalStateException("Invalid routing - taskId path parameter is missing")