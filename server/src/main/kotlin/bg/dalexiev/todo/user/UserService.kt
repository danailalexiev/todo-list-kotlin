package bg.dalexiev.todo.user

sealed class RegistrationResult {

    data class Success(val user: RegisteredUser) : RegistrationResult()

    data class DuplicateEmail(val email: String) : RegistrationResult()

}

interface UserService {

    suspend fun registerUser(user: UnregisteredUser): RegistrationResult

}

fun userService(repo: UserRepository): UserService = object : UserService {
    override suspend fun registerUser(user: UnregisteredUser): RegistrationResult =
        repo.create(user.email.value, user.password.hash().value)
            ?.let { RegistrationResult.Success(it) } ?: RegistrationResult.DuplicateEmail(user.email.value)
}