package bg.dalexiev.todo.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLog

actual fun chroniclerEngine(): ChroniclerEngine = object : ChroniclerEngine {

    private val dateFormatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    }

    override fun write(level: ChronicleLevel, tag: String, message: String) {
        NSLog("${getLogTime()} $tag ${level.name} - $message")
    }

    private fun getLogTime() = dateFormatter.stringFromDate(NSDate())
}