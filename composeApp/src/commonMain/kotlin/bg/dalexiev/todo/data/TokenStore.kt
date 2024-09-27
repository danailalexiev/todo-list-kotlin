package bg.dalexiev.todo.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenStore {

    val accessToken: StateFlow<String?>

    suspend fun addToken(accessToken: String?)
}

fun tokenStore(): TokenStore = object : TokenStore {

    private val _accessToken = MutableStateFlow<String?>(null)

    override val accessToken: StateFlow<String?>
        get() = _accessToken.asStateFlow()

    override suspend fun addToken(accessToken: String?) {
        _accessToken.emit(accessToken)
    }

}