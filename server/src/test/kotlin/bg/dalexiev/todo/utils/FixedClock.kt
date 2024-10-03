package bg.dalexiev.todo.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FixedClock(private val fixedAt: Instant): Clock {
    
    override fun now(): Instant = fixedAt

}

fun Clock.Companion.fixed(fixedAt: Instant): Clock = FixedClock(fixedAt)

