package bg.dalexiev.todo.login

import bg.dalexiev.todo.auth.AccessToken
import bg.dalexiev.todo.auth.LoginRequest
import bg.dalexiev.todo.auth.LoginResponse
import bg.dalexiev.todo.util.Either
import bg.dalexiev.todo.util.nonFatalOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface UserRepository {

    suspend fun login(email: String, password: String): Either<Throwable, AccessToken>

}

fun userRepository(httpClient: HttpClient, baseUrl: String): UserRepository =
    object : UserRepository {

        override suspend fun login(
            email: String,
            password: String
        ): Either<Throwable, AccessToken> =
            try {
                val response: LoginResponse = httpClient.post("$baseUrl/login") {
                    setBody(LoginRequest(email, password))
                }.body()
                Either.Success(AccessToken(response.token))
            } catch (e: Throwable) {
                Either.Failure(e.nonFatalOrThrow())
            }

    }