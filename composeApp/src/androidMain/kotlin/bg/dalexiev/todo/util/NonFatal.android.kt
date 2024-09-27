package bg.dalexiev.todo.util

import kotlin.coroutines.cancellation.CancellationException

actual fun isNonFatalError(e: Throwable): Boolean = 
    when (e) {
        is VirtualMachineError, is InterruptedException, is LinkageError, is CancellationException -> false
        else -> true
    }