package bg.dalexiev.todo.data

import bg.dalexiev.todo.auth.LoginRequest
import bg.dalexiev.todo.auth.LoginResponse
import bg.dalexiev.todo.util.Either
import bg.dalexiev.todo.util.catch
import bg.dalexiev.todo.util.nonFatalOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UserRepository {

    suspend fun login(email: String, password: String): Either<Throwable, String>

}

fun userRepository(httpClient: HttpClient, baseUrl: String): UserRepository =
    object : UserRepository {

        override suspend fun login(email: String, password: String): Either<Throwable, String> =
            try {
                val response: LoginResponse = httpClient.post("$baseUrl/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, password))
                }.body()
                Either.Success(response.token)
            } catch (e: Throwable) {
                Either.Failure(e.nonFatalOrThrow())
            }

    }