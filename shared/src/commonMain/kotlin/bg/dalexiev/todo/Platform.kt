package bg.dalexiev.todo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform