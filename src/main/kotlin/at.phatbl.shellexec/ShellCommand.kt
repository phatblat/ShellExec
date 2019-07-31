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

        private const val uninitializedExitValue = -999

        private const val bufferSize = 2 * 1024 * 1024
    }

    lateinit var process: Process

    var standardOutput: OutputStream? = null
    var errorOutput: OutputStream? = null
//    val standardInput: InputStream

    val stdout: String
        get() = stream2String(process.inputStream)

    val stderr: String
        get() = stream2String(process.errorStream)

    var exitValue: Int = uninitializedExitValue

    val succeeded: Boolean
        get() = exitValue == 0

    val failed: Boolean
        get() = !succeeded

    var readLimit = bufferSize

    /** Time allowed for command to run. Defaults to 20m */
    var timeout = defaultTimeout

    /**
     * Runs the command.
     */
    fun start() {
        baseDir.mkdirs()

        val pb = ProcessBuilder("bash", "-c", command)
            .directory(baseDir)

        // Launch the process
        process = pb.start()

        process.inputStream.mark(readLimit)
        process.errorStream.mark(readLimit)

        if (standardOutput != null) {
            copy(input = process.inputStream, output = standardOutput!!)
        }
        if (errorOutput != null) {
            copy(input = process.errorStream, output = errorOutput!!)
        }

        try {
            process.inputStream.reset()
        } catch (e: Exception) {
            val stdout = standardOutput
            if (stdout != null) {
                val errorMessage = "Could not reset input stream. Mark with Readlimit $readLimit not found.".toByteArray()
                stdout.write(errorMessage)
            }
        }

        try {
            process.errorStream.reset()
        } catch (e: Exception) {
            val stderr = errorOutput
            if (stderr != null) {
                val errorMessage = "Could not reset error stream. Mark with Readlimit $readLimit not found.".toByteArray()
                stderr.write(errorMessage)
            }
        }

        try {
            process.waitFor(timeout, TimeUnit.SECONDS)
            exitValue = process.exitValue()
        } catch (e: Exception) {
            // Handle timeouts
            println("ShellCommand timeout: $e")
        }
    }

    /**
     * Utility function which converts an input stream into a string.
     */
    private fun stream2String(stream: InputStream): String {
        return try {
            val reader = BufferedReader(InputStreamReader(stream))
            val builder = StringBuilder()
            val lineSeparator = System.getProperty("line.separator")
            reader.forEachLine { line ->
                builder.append(line)
                builder.append(lineSeparator)
            }

            builder.toString()
        } catch (e: Exception) {
            return ""
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
