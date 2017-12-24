package at.phatbl.simple_exec.logging

import org.apache.commons.exec.LogOutputStream
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.LogLevel

/**
 * Adapter which passes log output through to Gradle's logger.
 */
class GradleLogOutputStream(val logger: Logger, val level: LogLevel): LogOutputStream(level.ordinal) {
    /**
     * Logs a line to the log system of the user.
     *
     * @param line the line to log.
     * @param logLevel the log level to use
     */
    override fun processLine(line: String?, logLevel: Int) {
        if (line == null) return
        val level = when(logLevel) {
            0 -> LogLevel.DEBUG
            1 -> LogLevel.INFO
            2 -> LogLevel.LIFECYCLE
            3 -> LogLevel.WARN
            4 -> LogLevel.QUIET
            5 -> LogLevel.ERROR
            else -> throw GradleException("Unknown log level: $logLevel")
        }
        logger.log(level, line)
    }
}