package bg.dalexiev.todo.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.dalexiev.todo.data.TokenStore
import bg.dalexiev.todo.data.UserRepository
import bg.dalexiev.todo.resources.Res
import bg.dalexiev.todo.resources.login_title
import bg.dalexiev.todo.util.Either
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class LoginUiState(
    val isLoading: Boolean,
    val email: String,
    val password: String,
    val isSignInButtonEnabled: Boolean,
    val loginResult: LoginResult,
)

sealed interface LoginResult {

    data object NotAttempted : LoginResult
    data object Success : LoginResult
    data class Failure(val message: StringResource) : LoginResult
}

class LoginViewModel(
    private val userRepository: UserRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(
            isLoading = false,
            email = "",
            password = "",
            isSignInButtonEnabled = false,
            loginResult = LoginResult.NotAttempted
        )
    )
    val uiState: StateFlow<LoginUiState>
        get() = _uiState.asStateFlow()

    private var email by mutableStateOf("")

    private var password by mutableStateOf("")

    init {
        snapshotFlow { email to password }
            .mapLatest {
                Triple(it.first, it.second, it.first.isNotBlank() && it.second.isNotBlank())
            }
            .onEach { formState ->
                _uiState.update {
                    it.copy(
                        email = formState.first,
                        password = formState.second,
                        isSignInButtonEnabled = formState.third,
                        loginResult = LoginResult.NotAttempted
                    )
                }
            }.launchIn(viewModelScope)

    }

    fun onEmailChanged(input: String) {
        email = input
    }

    fun onPasswordChanged(input: String) {
        password = input
    }

    fun onSignInButtonClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSignInButtonEnabled = false) }

            when (val result = userRepository.login(email, password)) {
                is Either.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignInButtonEnabled = true,
                        loginResult = LoginResult.Failure(Res.string.login_title)
                    )
                }

                is Either.Success -> {
                    tokenStore.addToken(result.value)
                    _uiState.update { it.copy(loginResult = LoginResult.Success) }
                }
            }

        }
    }
}