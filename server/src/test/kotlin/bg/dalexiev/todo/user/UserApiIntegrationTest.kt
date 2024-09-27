package bg.dalexiev.todo.user

import bg.dalexiev.todo.utils.ApiIntegrationTestSpec
import bg.dalexiev.todo.utils.JdbcTemplate
import bg.dalexiev.todo.utils.appScenario
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.mindrot.jbcrypt.BCrypt

private val invalidRequests = listOf(
    "email is missing" to RegisterUserRequest("", "Secret!23"),
    "email is invalid" to RegisterUserRequest("test", "Secret!23"),
    "password is missing" to RegisterUserRequest("test@test.com", ""),
    "password is too short" to RegisterUserRequest("test@test.com", "Se!23"),
    "password does not contain upper case letters" to RegisterUserRequest("test@test.com", "secret!23"),
    "password does not contain lower case letters" to RegisterUserRequest("test@test.com", "SECRET!23"),
    "password does not contain number" to RegisterUserRequest("test@test.com", "Secret!!!"),
    "password does not contain special character" to RegisterUserRequest("test@test.com", "Secret123"),
)

class UserApiIntegrationTest : ApiIntegrationTestSpec({
    feature("POST /users") {

        invalidRequests.forEach {
            appScenario("Returns 400 Bad Request if ${it.first}") {
                val response = post("/users") {
                    contentType(ContentType.Application.Json)

                    setBody(it.second)
                }

                response shouldHaveStatus HttpStatusCode.BadRequest
            }
        }
        
        appScenario("Return 400 Bad Request if email already in use") {
            val email = "test@test.com"
            val password = "Secret!23"
            insertUser("test", password)
            
            val response = post("/users") {
                contentType(ContentType.Application.Json)
                
                setBody(RegisterUserRequest(email, password))
            }
            
            response shouldHaveStatus HttpStatusCode.BadRequest
        }

        appScenario("Returns 201 Created") {
            val response = post("/users") {
                contentType(ContentType.Application.Json)

                setBody(RegisterUserRequest("test@test.com", "Secret!23"))
            }

            response shouldHaveStatus HttpStatusCode.Created
            response.body<RegisterUserResponse>() shouldBe RegisterUserResponse(1L, "test@test.com")
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