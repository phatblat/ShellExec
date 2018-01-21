package at.phatbl.shellexec

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.concurrent.TimeUnit

/**
 * Wrapper for running several commands inside a Bash shell.
 */
data class ShellCommand(
        val baseDir: File,
        val command: String
) {
    companion object {
        // 20m
        private const val timeout = 1200L

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

    /**
     * Runs the command.
     */
    fun start() {
        baseDir.mkdir()
        val pb = ProcessBuilder("bash", "-c", "cd $baseDir && $command")
        process = pb.start()

        if (standardOutput != null) {
            copy(input = process.inputStream, output = standardOutput!!)
        }
        if (errorOutput != null) {
            copy(input = process.errorStream, output = errorOutput!!)
        }

        process.waitFor(timeout, TimeUnit.SECONDS)
        exitValue = process.exitValue()
    }

    /**
     * Utility function which converts an input stream into a string.
     */
    private fun stream2String(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        val lineSeparator = System.getProperty("line.separator")
        reader.forEachLine { line ->
            builder.append(line)
            builder.append(lineSeparator)
        }
        return builder.toString()
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
        } finally {
            input.close()
            output.close()
        }
    }
}
