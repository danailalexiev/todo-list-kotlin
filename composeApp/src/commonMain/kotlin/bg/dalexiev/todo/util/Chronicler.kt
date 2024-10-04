package bg.dalexiev.todo.util

enum class ChronicleLevel {
    INFO, DEBUG, VERBOSE, WARNING, ERROR
}

interface ChroniclerEngine {
    
    fun write(level: ChronicleLevel, tag: String, message: String)
    
}

expect fun chroniclerEngine(): ChroniclerEngine

object Chronicler {

    private lateinit var engine: ChroniclerEngine

    fun engine(engine: ChroniclerEngine) {
        this.engine = engine
    }

    fun info(tag: String, message: String) {
        engine.write(ChronicleLevel.INFO, tag, message)
    }
    
    fun debug(tag: String, message: String) {
        engine.write(ChronicleLevel.DEBUG, tag, message)
    }
    
    fun verbose(tag: String, message: String) {
        engine.write(ChronicleLevel.VERBOSE, tag, message)
    }
    
    fun warning(tag: String, message: String) {
        engine.write(ChronicleLevel.WARNING, tag, message)
    }
    
    fun error(tag: String, message: String) {
        engine.write(ChronicleLevel.ERROR, tag, message)
    }
}