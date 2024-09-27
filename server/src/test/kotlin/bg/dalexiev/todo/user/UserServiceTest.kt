package bg.dalexiev.todo.user

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.mindrot.jbcrypt.BCrypt

class UserServiceTest : FunSpec({

    val repo = mockk<UserRepository>()

    val service = userService(repo)

    context("registerUser") {
        val email = "test@test.com"
        val password = "Secret!23"

        val unregisteredUser = UnregisteredUser(
            email = EmailAddress(email),
            password = Password(password)
        )

        test("returns Success") {
            // given
            val registeredUser = RegisteredUser(
                id = 1L,
                email = EmailAddress(email),
                password = HashedPassword("hash")
            )

            coEvery { repo.create(any<String>(), any<String>()) } returns registeredUser

            // when
            val actualResult = service.registerUser(unregisteredUser)

            // then
            actualResult shouldBe RegistrationResult.Success(registeredUser)

            coVerify { repo.create(email, withArg { BCrypt.checkpw(password, it) }) }
        }

        test("returns DuplicateEmail") {
            // given
            coEvery { repo.create(any<String>(), any<String>()) } returns null

            // when
            val actualResult = service.registerUser(unregisteredUser)

            // then
            actualResult shouldBe RegistrationResult.DuplicateEmail(email)

            coVerify { repo.create(email, withArg { BCrypt.checkpw(password, it) }) }
        }
    }

})