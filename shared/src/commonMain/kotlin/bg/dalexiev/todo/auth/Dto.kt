package bg.dalexiev.todo.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String, 
    @SerialName("password") val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String
)