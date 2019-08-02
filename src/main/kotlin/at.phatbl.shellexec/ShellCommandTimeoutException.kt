package at.phatbl.shellexec

/**
 * Checked exception thrown when the underlying command's process exceeds the configured timeout.
 */
class ShellCommandTimeoutException(
    message: String,
    cause: Throwable
) : Exception(message, cause)
