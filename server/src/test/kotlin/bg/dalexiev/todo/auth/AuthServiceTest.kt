package bg.dalexiev.todo.auth

import bg.dalexiev.todo.Environment
import bg.dalexiev.todo.user.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.auth.jwt.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.days

class AuthServiceTest : FunSpec({

    val repo = mockk<UserRepository>()

    val authConfig = Environment.Auth(
        secret = "secret",
        issuer = "issuer",
        duration = 10.days
    )

    val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    val service = authService(authConfig, repo, fixedClock)

    context("login") {
        val email = EmailAddress("test@test.com")
        val password = Password("Secret!23")

        test("returns failure if the user does not exist") {
            // given
            coEvery { repo.findByEmail(any<String>()) } returns null

            // when
            val actualResult = service.login(email, password)

            // then
            actualResult shouldBe LoginResult.Failure
        }

        test("returns failure if the passwords don't match") {
            // given
            val hashedPassword = Password("Secret!45").hash()

            coEvery { repo.findByEmail(any<String>()) } returns RegisteredUser(
                id = 1L,
                email = email,
                password = hashedPassword
            )

            // when
            val actualResult = service.login(email, password)

            // then
            actualResult shouldBe LoginResult.Failure
        }

        test("returns success") {
            // given
            val hashedPassword = password.hash()

            coEvery { repo.findByEmail(any<String>()) } returns RegisteredUser(
                id = 1L,
                email = email,
                password = hashedPassword
            )

            val token = JWT.create()
                .withIssuer(authConfig.issuer)
                .withExpiresAt(Instant.now(fixedClock).plusSeconds(authConfig.duration.inWholeSeconds))
                .withClaim("id", 1L)
                .sign(Algorithm.HMAC256(authConfig.secret))

            // when
            val actualResult = service.login(email, password)

            // then
            actualResult shouldBe LoginResult.Success(token)
        }
    }

    context("validateCredential") {
        test("returns null if no expiry date") {
            // given
            val credential = JWTCredential(
                payload = mockk<Payload>().apply {
                    every { expiresAtAsInstant } returns null
                }
            )

            // when
            val actualResult = service.validateCredential(credential)

            // then
            actualResult shouldBe null
        }

        test("returns null if token has expired") {
            // given
            val credential = JWTCredential(
                payload = mockk<Payload>().apply {
                    every { expiresAtAsInstant } returns Instant.now(fixedClock).minusSeconds(600L)
                }
            )

            // when
            val actualResult = service.validateCredential(credential)

            // then
            actualResult shouldBe null
        }

        test("returns null if no id claim") {
            // given
            val credential = JWTCredential(
                payload = mockk<Payload>().apply {
                    every { expiresAtAsInstant } returns Instant.now(fixedClock).plusSeconds(600L)
                    every { getClaim("id") } returns null
                }
            )

            // when
            val actualResult = service.validateCredential(credential)

            // then
            actualResult shouldBe null
        }

        test("returns null if id claim not a long") {
            // given
            val credential = JWTCredential(
                payload = mockk<Payload>().apply {
                    every { expiresAtAsInstant } returns Instant.now(fixedClock).plusSeconds(600L)
                    every { getClaim("id") } returns mockk<Claim>().apply {
                        every { asLong() } returns null
                    }
                }
            )

            // when
            val actualResult = service.validateCredential(credential)

            // then
            actualResult shouldBe null
        }

        test("returns null if user does not exist") {
            // given
            val claimId = 1L;
            val credential = JWTCredential(
                payload = mockk<Payload>().apply {
                    every { expiresAtAsInstant } returns Instant.now(fixedClock).plusSeconds(600L)
                    every { getClaim("id") } returns mockk<Claim>().apply {
                        every { asLong() } returns claimId
                    }
                }
            )

            coEvery { repo.findById(any<Long>()) } returns null

            // when
            val actualResult = service.validateCredential(credential)

            // then
            actualResult shouldBe null

            coVerify { repo.findById(claimId) }
        }

        test("returns principal") {
            // given
            val claimId = 1L;
            val payload = mockk<Payload>().apply {
                every { expiresAtAsInstant } returns Instant.now(fixedClock).plusSeconds(600L)
                every { getClaim("id") } returns mockk<Claim>().apply {
                    every { asLong() } returns claimId
                }
            }
            val credential = JWTCredential(payload)

            coEvery { repo.findById(any<Long>()) } returns RegisteredUser(
                id = claimId,
                email = EmailAddress("test@test.com"),
                password = HashedPassword("secret")
            )

            // when
            val actualResult = service.validateCredential(credential)

            // then
            actualResult!!.payload shouldBe payload

            coVerify { repo.findById(claimId) }
        }
    }

})