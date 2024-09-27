@file:OptIn(ExperimentalContracts::class)

package bg.dalexiev.todo.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class Either<out E, out R> {

    data class Success<out R>(val value: R) : Either<Nothing, R>()

    data class Failure<out E>(val error: E) : Either<E, Nothing>()

}

fun <T, R> Either<Throwable, T>.mapCatching(mapper: (T) -> R): Either<Throwable, R> =
    when (this) {
        is Either.Failure -> this
        is Either.Success -> catch { mapper(value) }
    }

inline fun <R> catch(block: () -> R): Either<Throwable, R> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        Either.Success(block())
    } catch (e: Throwable) {
        Either.Failure(e.nonFatalOrThrow())
    }
}
