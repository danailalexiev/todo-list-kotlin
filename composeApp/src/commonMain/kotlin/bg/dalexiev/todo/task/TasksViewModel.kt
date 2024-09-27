package bg.dalexiev.todo.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.dalexiev.todo.auth.TokenStore
import bg.dalexiev.todo.util.Either
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TasksUiState {

    data object NotLoggedIn : TasksUiState

    sealed interface LoggedIn : TasksUiState {

        val isLoading: Boolean

        data class Loaded(
            override val isLoading: Boolean,
            val tasks: List<Task>
        ) : LoggedIn

        data class AllDone(override val isLoading: Boolean) : LoggedIn

        data class LoadingFailed(override val isLoading: Boolean) : LoggedIn
    }

}

class TasksViewModel(private val taskRepo: TaskRepository, tokenStore: TokenStore) :
    ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.LoggedIn.AllDone(true))
    val uiState: StateFlow<TasksUiState>
        get() = _uiState.asStateFlow()

    init {
        tokenStore.accessToken
            .map {
                if (it == null) {
                    TasksUiState.NotLoggedIn
                } else {
                    TasksUiState.LoggedIn.AllDone(isLoading = true)
                }
            }
            .onEach { updateUiState ->
                _uiState.update { updateUiState }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                when (it) {
                    is TasksUiState.LoggedIn.AllDone -> it.copy(isLoading = true)
                    is TasksUiState.LoggedIn.Loaded -> it.copy(isLoading = true)
                    is TasksUiState.LoggedIn.LoadingFailed -> it.copy(isLoading = true)
                    TasksUiState.NotLoggedIn -> it
                }
            }

            val uiState = when (val result = taskRepo.getTasks()) {
                is Either.Failure -> TasksUiState.LoggedIn.LoadingFailed(false)
                is Either.Success -> if (result.value.isEmpty()) {
                    TasksUiState.LoggedIn.AllDone(false)
                } else {
                    TasksUiState.LoggedIn.Loaded(false, result.value)
                }
            }
            _uiState.update { uiState }
        }
    }
}