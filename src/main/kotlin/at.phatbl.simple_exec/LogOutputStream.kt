package at.phatbl.simple_exec

import java.io.ByteArrayOutputStream
import org.gradle.api.logging.Logger
import org.gradle.api.logging.LogLevel

class LogOutputStream(val logger: Logger, val level: LogLevel) : ByteArrayOutputStream() {
    override fun flush() {
        logger.log(level, toString())
        reset()
    }
}
