package bg.dalexiev.todo.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenStore {

    val accessToken: StateFlow<AccessToken?>

    suspend fun addToken(accessToken: AccessToken?)
}

fun tokenStore(): TokenStore = object : TokenStore {

    private val _accessToken = MutableStateFlow<AccessToken?>(null)

    override val accessToken: StateFlow<AccessToken?>
        get() = _accessToken.asStateFlow()

    override suspend fun addToken(accessToken: AccessToken?) {
        _accessToken.emit(accessToken)
    }

}