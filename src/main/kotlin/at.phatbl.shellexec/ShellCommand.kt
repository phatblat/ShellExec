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
        private const val uninitializedExitValue = -999

        private const val bufferSize = 2 * 1024 * 1024
    }

    lateinit var process: Process

    var standardOutput: OutputStream? = null
    var errorOutput: OutputStream? = null

    private var outputFile: File? = null
    private var errorFile: File? = null

    val stdout: String
        get() {
            val output = outputFile ?: return ""
            return output.bufferedReader().use { it.readText() }
        }

    val stderr: String
        get() {
            val errors = errorFile ?: return ""
            return errors.bufferedReader().use { it.readText() }
        }

    var exitValue: Int = uninitializedExitValue

    val succeeded: Boolean
        get() = exitValue == 0

    val failed: Boolean
        get() = !succeeded

    /** Time allowed for command to run. Defaults to 20m */
    var timeout = defaultTimeout

    /**
     * Runs the command.
     */
    fun start() {
        baseDir.mkdirs()

        val outputStream = standardOutput
        val errorStream = errorOutput

        val pb = ProcessBuilder("bash", "-c", command)
            .directory(baseDir)

        if (outputStream == null) {
            outputFile = createTempFile("shellexec-", "-output.log", baseDir)
            pb.redirectOutput(outputFile)
        }
        if (errorStream == null) {
            errorFile = createTempFile("shellexec-", "-error.log", baseDir)
            pb.redirectError(errorFile)
        }

        // Launch the process
        process = pb.start()

        if (outputStream != null) {
            copy(input = process.inputStream, output = outputStream)
        }
        if (errorStream != null) {
            copy(input = process.errorStream, output = errorStream)
        }

        try {
            process.waitFor(timeout, TimeUnit.SECONDS)
            exitValue = process.exitValue()
        } catch (e: Exception) {
            // Handle timeouts
            println("ShellCommand timeout: $e")
        }
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream) {
        try {
            val buffer = ByteArray(bufferSize)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
            //If needed, close streams.
        } finally { }
    }
}
