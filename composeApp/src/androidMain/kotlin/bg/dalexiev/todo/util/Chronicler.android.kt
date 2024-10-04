package bg.dalexiev.todo.util

import android.util.Log

actual fun chroniclerEngine(): ChroniclerEngine = object: ChroniclerEngine {

    override fun write(level: ChronicleLevel, tag: String, message: String) {
        when (level) {
            ChronicleLevel.INFO -> Log.i(tag, message)
            ChronicleLevel.DEBUG -> Log.d(tag, message)
            ChronicleLevel.VERBOSE -> Log.v(tag, message)
            ChronicleLevel.WARNING -> Log.w(tag, message)
            ChronicleLevel.ERROR -> Log.e(tag, message)
        }
    }

}