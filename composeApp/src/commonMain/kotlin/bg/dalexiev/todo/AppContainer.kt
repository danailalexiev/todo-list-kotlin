package bg.dalexiev.todo

import bg.dalexiev.todo.auth.tokenStore
import bg.dalexiev.todo.login.userRepository
import bg.dalexiev.todo.task.fakeTaskRepository
import bg.dalexiev.todo.util.Chronicler
import bg.dalexiev.todo.util.chroniclerEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

const val BASE_URL = "https://17fb-77-78-138-47.ngrok-free.app"

class AppContainer {

    init {
        Chronicler.engine(chroniclerEngine())
    }

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
                    tokenStore.accessToken.value?.let { BearerTokens(it.value, null) }
                }
            }
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = object: Logger {
                override fun log(message: String) {
                    Chronicler.debug("HTTP Client", message)
                }

            }
        }

    }

    val userRepository = userRepository(httpClient, BASE_URL)
    val taskRepository = fakeTaskRepository()
}