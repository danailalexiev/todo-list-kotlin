package bg.dalexiev.todo.auth

import bg.dalexiev.todo.Environment
import bg.dalexiev.todo.user.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import java.time.Clock
import java.time.Instant

sealed class LoginResult {

    data class Success(val token: String) : LoginResult()

    data object Failure : LoginResult()

}

interface AuthService {

    suspend fun login(email: EmailAddress, password: Password): LoginResult

    suspend fun validateCredential(credential: JWTCredential): JWTPrincipal?

}

fun authService(authConfig: Environment.Auth, repo: UserRepository, clock: Clock = Clock.systemUTC()) = object : AuthService {

    override suspend fun login(email: EmailAddress, password: Password): LoginResult =
        repo.findByEmail(email.value)?.let { validatePassword(it.id, it.password, password) }
            ?: LoginResult.Failure

    private fun validatePassword(id: Long, hashedPassword: HashedPassword, password: Password): LoginResult =
        if (hashedPassword.matchesPassword(password)) {
            LoginResult.Success(generateJwt(id, authConfig))
        } else {
            LoginResult.Failure
        }

    private fun generateJwt(id: Long, authConfig: Environment.Auth): String =
        JWT.create()
            .withIssuer(authConfig.issuer)
            .withExpiresAt(Instant.now(clock).plusSeconds(authConfig.duration.inWholeSeconds))
            .withClaim("id", id)
            .sign(Algorithm.HMAC256(authConfig.secret))

    override suspend fun validateCredential(credential: JWTCredential): JWTPrincipal? =
        if (credential.isStillActive() && credential.isIssuedToValidUser()) {
            JWTPrincipal(payload = credential.payload)
        } else {
            null
        }
    
    private fun JWTCredential.isStillActive(): Boolean = payload.expiresAtAsInstant?.isAfter(Instant.now(clock)) ?: false
    
    private suspend fun JWTCredential.isIssuedToValidUser(): Boolean =
        payload.getClaim("id")?.asLong()?.let { repo.findById(it)?.let { true } } ?: false
        
}