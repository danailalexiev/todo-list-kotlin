package bg.dalexiev.todo

import bg.dalexiev.todo.data.fakeTaskRepository
import bg.dalexiev.todo.data.tokenStore
import bg.dalexiev.todo.data.userRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

const val BASE_URL = "https://2188-77-78-138-47.ngrok-free.app"

class AppContainer {

    val tokenStore = tokenStore()

    private val httpClient = HttpClient {

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 10000L
            requestTimeoutMillis = 2000L
        }

        install(ContentNegotiation) {
            json()
        }

        install(Auth) {
            bearer {
                loadTokens {
                    tokenStore.accessToken.value?.let { BearerTokens(it, "") }
                }
            }
        }

    }

    val userRepository = userRepository(httpClient, BASE_URL)
    val taskRepository = fakeTaskRepository()
}