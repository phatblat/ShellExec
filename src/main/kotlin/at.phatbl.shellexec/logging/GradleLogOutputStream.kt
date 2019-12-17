package at.phatbl.shellexec.logging

import org.apache.commons.exec.LogOutputStream
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File

/**
 * Adapter which passes log output through to Gradle's logger.
 */
class GradleLogOutputStream(val logger: Logger, level: LogLevel, val logFiles: Array<File>? = null, val isLoggingTo: LoggingMethod = LoggingMethod.BOTH): LogOutputStream(level.ordinal) {
    /**
     * Logs a line to the log system of the user.
     *
     * @param line the line to log.
     * @param logLevel the log level to use
     */
    override fun processLine(line: String?, logLevel: Int) {
        if (line == null) return
        val level = when(logLevel) {
            LogLevel.DEBUG.ordinal -> LogLevel.DEBUG
            LogLevel.INFO.ordinal -> LogLevel.INFO
            LogLevel.LIFECYCLE.ordinal -> LogLevel.LIFECYCLE
            LogLevel.WARN.ordinal -> LogLevel.WARN
            LogLevel.QUIET.ordinal -> LogLevel.QUIET
            LogLevel.ERROR.ordinal -> LogLevel.ERROR
            else -> throw GradleException("Unknown log level: $logLevel")
        }

        when(isLoggingTo) {
            LoggingMethod.BOTH -> {
                logFiles?.forEach { it.appendText("$line\n") }
                logger.log(level, line)
            }
            LoggingMethod.FILE -> {
                logFiles?.forEach { it.appendText("$line\n") }
            }
            LoggingMethod.STDOUT -> {
                logger.log(level, line)
            }
        }
    }
}

enum class LoggingMethod {
    FILE,
    STDOUT,
    BOTH
}
