package bg.dalexiev.todo.di

import bg.dalexiev.todo.Environment
import bg.dalexiev.todo.auth.AuthService
import bg.dalexiev.todo.auth.authService
import bg.dalexiev.todo.user.UserRepository
import bg.dalexiev.todo.user.UserService
import bg.dalexiev.todo.user.userService
import javax.sql.DataSource

class Dependencies(environment: Environment, dataSource: DataSource? = null) {

    val dataSource: DataSource = dataSource ?: hikari(environment)

    private val userRepository: UserRepository = UserRepository()
    val userService: UserService = userService(userRepository)

    val authService: AuthService = authService(environment.auth, userRepository)
}