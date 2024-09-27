package bg.dalexiev.todo.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    @SerialName("email") val email: String, 
    @SerialName("password") val password: String
)

@Serializable
data class RegisterUserResponse(
    @SerialName("id") val id: Long, 
    @SerialName("email") val email: String
)