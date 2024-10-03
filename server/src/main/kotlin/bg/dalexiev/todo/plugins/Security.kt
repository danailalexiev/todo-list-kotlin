package bg.dalexiev.todo.plugins

import bg.dalexiev.todo.Environment
import bg.dalexiev.todo.auth.AuthService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(authConfig: Environment.Auth, authService: AuthService) {
    authentication {
        jwt("jwt-auth") {
            realm = authConfig.issuer
            verifier(
                JWT
                    .require(Algorithm.HMAC256(authConfig.secret))
                    .withIssuer(authConfig.issuer)
                    .build()
            )
            validate { authService.validateCredential(it) }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
