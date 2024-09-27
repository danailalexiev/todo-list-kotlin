package bg.dalexiev.todo.util

expect fun isNonFatalError(e: Throwable): Boolean

fun Throwable.nonFatalOrThrow(): Throwable =
    if (isNonFatalError(this)) this else throw this