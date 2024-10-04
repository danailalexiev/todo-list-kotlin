package bg.dalexiev.todo.util

actual fun chroniclerEngine(): ChroniclerEngine = object : ChroniclerEngine {

    override fun write(level: ChronicleLevel, tag: String, message: String) {

        val fullMessage = "${getLogTime()} $tag ${level.name} - $message"

        when (level) {
            ChronicleLevel.INFO -> printInfo(fullMessage)
            ChronicleLevel.DEBUG -> println(fullMessage)
            ChronicleLevel.VERBOSE -> println(fullMessage)
            ChronicleLevel.WARNING -> printWarn(fullMessage)
            ChronicleLevel.ERROR -> printError(fullMessage)
        }
    }

}

private fun printInfo(message: String): Unit = js("console.info(message)")

private fun printWarn(message: String): Unit = js("console.warn(message)")

private fun printError(message: String): Unit = js("console.error(message)")

private fun getLogTime(): String = js("new Date().toISOString()")