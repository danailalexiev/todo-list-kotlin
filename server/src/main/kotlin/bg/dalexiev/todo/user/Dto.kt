package bg.dalexiev.todo.user

fun RegisterUserRequest.toUnregisteredUser(): UnregisteredUser =
    UnregisteredUser(
        email = EmailAddress(email),
        password = Password(password)
    )

fun RegisteredUser.toResponse(): RegisterUserResponse =
    RegisterUserResponse(
        id = this.id,
        email = this.email.value
    )