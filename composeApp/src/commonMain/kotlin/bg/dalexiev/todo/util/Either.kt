package bg.dalexiev.todo.util

sealed class Either<out E, out R> {

    data class Success<out R>(val value: R) : Either<Nothing, R>()

    data class Failure<out E>(val error: E) : Either<E, Nothing>()

}

inline fun <R> catch(block: () -> R): Either<Throwable, R> {
    return try {
        Either.Success(block())
    } catch (e: Throwable) {
        Either.Failure(e.nonFatalOrThrow())
    }
}
