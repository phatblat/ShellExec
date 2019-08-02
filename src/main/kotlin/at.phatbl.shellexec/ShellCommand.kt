package at.phatbl.shellexec

import java.io.*
import java.util.concurrent.TimeUnit

/**
 * Wrapper for running several commands inside a Bash shell.
 */
data class ShellCommand(
        /** Working dir for command. Defaults to the JVM's current directory. */
        val baseDir: File = File("."),
        val command: String
) {
    companion object {
        // 20m
        private const val defaultTimeout: Long = 1200

        // Used to track whether we've received an updated exit code from the process.
        const val uninitializedExitValue = -999

        private const val bufferSize = 2 * 1024 * 1024
    }

    /**
     * Time allowed for command to run. Defaults to 20m
     */
    var timeout = defaultTimeout

    /**
     * Exposes the exit code of the underlying process. Defaults to -999 until the process has exited.
     */
    var exitValue: Int = uninitializedExitValue

    /**
     * True if the process ended with a successful exit code.
     */
    val succeeded: Boolean
        get() = exitValue == 0

    /**
     * False if the process did not return a successful exit code.
     */
    val failed: Boolean
        get() = !succeeded

    /**
     * Exposes a stream of command output from the underlying process if you provide an OutputStream.
     * Mutually exclusive with stdout. Defaults to null.
     * @see stdout
     */
    var standardOutput: OutputStream? = null

    /**
     * Exposes a stream of command error output from the underlying process if you provide an OutputStream.
     * Mutually exclusive with stderr. Defaults to null.
     * @see stderr
     */
    var errorOutput: OutputStream? = null

    /**
     * Convenience property for accessing stdout from command as a string after the command has finished running.
     * Will always be null if you provide an OutputStream to standardOutput.
     * @see standardOutput
     */
    val stdout: String?
        get() {
            val output = outputFile ?: return null
            return output.bufferedReader().use { it.readText() }
        }

    /**
     * Convenience property for accessing stderr from command as a string after the command has finished running.
     * Will always be null if you provide an OutputStream to errorOutput.
     * @see errorOutput
     */
    val stderr: String?
        get() {
            val errors = errorFile ?: return null
            return errors.bufferedReader().use { it.readText() }
        }

    /**
     * File containing stdout from command. null if an OutputStream is provided to standardOutput before start is called.
     */
    private var outputFile: File? = null

    /**
     * File containing stderr from command. null if an OutputStream is provided to errorOutput before start is called.
     */
    private var errorFile: File? = null

    /**
     * Runs the command.
     *
     * @throws ShellCommandTimeoutException if the command process did not finish before the timeout.
     */
    fun start() {
        baseDir.mkdirs()

        val pb = ProcessBuilder("bash", "-c", command)
            .directory(baseDir)

        val outputStream = standardOutput
        val errorStream = errorOutput

        if (outputStream == null) {
            outputFile = createTempFile("shellexec-", "-output.log")
            pb.redirectOutput(outputFile)
        }
        if (errorStream == null) {
            errorFile = createTempFile("shellexec-", "-error.log")
            pb.redirectError(errorFile)
        }

        // Launch the process
        val process = pb.start()

        if (outputStream != null) {
            copy(input = process.inputStream, output = outputStream)
        }
        if (errorStream != null) {
            copy(input = process.errorStream, output = errorStream)
        }

        try {
            process.waitFor(timeout, TimeUnit.SECONDS)
            exitValue = process.exitValue()
        } catch (e: InterruptedException) {
            val message = "Command timeout, exceeded $timeout second limit."
            throw ShellCommandTimeoutException(message, e)
        }
    }

    /**
     * Passes characters from the input stream to the output stream.
     */
    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream) {
        input.use {
            output.use {
                val buffer = ByteArray(bufferSize)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    output.write(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
        }
    }
}
