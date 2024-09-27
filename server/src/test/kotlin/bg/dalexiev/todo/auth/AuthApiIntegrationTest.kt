package bg.dalexiev.todo.auth

import bg.dalexiev.todo.utils.ApiIntegrationTestSpec
import bg.dalexiev.todo.utils.JdbcTemplate
import bg.dalexiev.todo.utils.appScenario
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.mindrot.jbcrypt.BCrypt

private val invalidRequests = listOf(
    "email is missing" to LoginRequest("", "Secret!23"),
    "email is invalid" to LoginRequest("test", "Secret!23"),
    "password is missing" to LoginRequest("test@test.com", ""),
    "password is too short" to LoginRequest("test@test.com", "Se!23"),
    "password does not contain upper case letters" to LoginRequest("test@test.com", "secret!23"),
    "password does not contain lower case letters" to LoginRequest("test@test.com", "SECRET!23"),
    "password does not contain number" to LoginRequest("test@test.com", "Secret!!!"),
    "password does not contain special character" to LoginRequest("test@test.com", "Secret123"),
)

class AuthApiIntegrationTest : ApiIntegrationTestSpec({
    feature("POST /login") {
        val email = "test@test.com"
        val password = "Secret!23"
        
        invalidRequests.forEach {
            appScenario("Returns 400 Bad Request if ${it.first}") {
                val response = post("/login") {
                    contentType(ContentType.Application.Json)

                    setBody(it.second)
                }

                response shouldHaveStatus HttpStatusCode.BadRequest
            }
        }

        appScenario("Returns 400 Bad Request if wrong email provided") {
            insertUser(email, password)

            val response = post("/login") {
                contentType(ContentType.Application.Json)

                setBody(LoginRequest("missingUser@test.com", "Secret!23"))
            }

            response shouldHaveStatus HttpStatusCode.BadRequest
        }
        
        appScenario("Returns 400 Bad Request if wrong password provided") {
            insertUser(email, password)

            val response = post("/login") {
                contentType(ContentType.Application.Json)

                setBody(LoginRequest("test@test.com", "Secret@34"))
            }

            response shouldHaveStatus HttpStatusCode.BadRequest
        }
        
        appScenario("Returns 200 OK") {
            insertUser(email, password)
            
            val response = post("/login") {
                contentType(ContentType.Application.Json)

                setBody(LoginRequest("test@test.com", "Secret!23"))
            }
            
            response shouldHaveStatus HttpStatusCode.OK
            response.body<LoginResponse>().token shouldNotBeNull {}
        }
    }

})

private fun JdbcTemplate.insertUser(email: String, password: String) {
    insert(
        "insert into users(email, password) values (?, ?)",
        listOf(email, BCrypt.hashpw(password, BCrypt.gensalt()))
    ) {
        it.getLong(1)
    }
}