package at.phatbl.shellexec

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile

/**
 * Wrapper for running several commands inside a Bash shell.
 */
open class ShellCommand(
    /** Working dir for command. Defaults to the JVM's current directory. */
    val baseDir: File = File("."),
    val command: String,
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
    open val stdout: String?
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
     * File where stdout will be written. null if an OutputStream is provided to standardOutput before start is called.
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
    open fun start() {
        baseDir.mkdirs()

        val pb =
            ProcessBuilder("bash", "-c", command)
                .directory(baseDir)

        val outputStream = standardOutput
        val errorStream = errorOutput

        if (outputStream == null) {
            val path = createTempFile("shellexec-", "-output.log")
            pb.redirectOutput(path.toFile())
            outputFile = path.toFile()
        }
        if (errorStream == null) {
            val path = createTempFile("shellexec-", "-error.log")
            pb.redirectError(path.toFile())
            errorFile = path.toFile()
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

            // Check to see if the process has quit. Otherwise, calling exitValue throws IllegalThreadStateException
            if (!process.isAlive) {
                exitValue = process.exitValue()
            }
        } catch (e: InterruptedException) {
            val message = "Command timeout, exceeded $timeout second limit."
            throw ShellCommandTimeoutException(message, e)
        }
    }

    /**
     * Passes characters from the input stream to the output stream.
     */
    @Throws(IOException::class)
    private fun copy(
        input: InputStream,
        output: OutputStream,
    ) {
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
