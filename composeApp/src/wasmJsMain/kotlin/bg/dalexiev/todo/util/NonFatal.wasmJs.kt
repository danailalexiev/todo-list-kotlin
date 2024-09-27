package bg.dalexiev.todo.util

import kotlin.coroutines.cancellation.CancellationException

actual fun isNonFatalError(e: Throwable): Boolean =
    when (e) {
        is CancellationException -> false
        else -> true
    }