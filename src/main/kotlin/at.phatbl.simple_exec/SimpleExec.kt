package at.phatbl.simple_exec

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import java.io.File
import java.io.InputStream
import java.io.OutputStream

open class SimpleExec: DefaultTask() { //, GradleExec<SimpleExec> {
    companion object {
        // Directories to be prepended to PATH
        private const val pathAdditions = "./bin:/usr/local/bin"
        private const val PATH = "PATH"
    }

    var commandLine = listOf<String>()
    var executable: String? = null
    var args = listOf<String>()
    var environment = mutableMapOf<String, Any>()
    lateinit var workingDir: File

    val standardInput: OutputStream
        get() = shellCommand.process.outputStream

    val standardOutput: InputStream
        get() = shellCommand.process.inputStream

    val errorOutput: InputStream
        get() = shellCommand.process.errorStream

    var ignoreExitValue: Boolean = false
    var execResult: ExecResult? = null

    val exitValue: Int
        get() = shellCommand.exitValue

    private lateinit var shellCommand: ShellCommand

    /**
     * String of commands to be executed by Gradle, split on space before being passed to commandLine.
     */
    @Input
    var command: String = ""
        get() {
            field = commandLine.joinToString(" ")
            return field
        }
        set(value) {
            field = value
            commandLine = field.trim().split(" ")
        }

    /** Property containing a copy of the PATH environment variable. */
    @Input
    protected var systemPath: String

    /** Value to be prepended to the PATH. */
//    @Input
    protected var prePath: String? = null
        get() = field
        set(value) {
            field = value
            buildPath()
        }

    /** Value to be appended to the PATH. */
//    @Input
    protected var postPath: String? = null
        get() = field
        set(value) {
            field = value
            buildPath()
        }

    init {
        systemPath = System.getenv(PATH)
    }

    @TaskAction
    fun exec() {
        if (workingDir == null) {
            throw GradleException("workingDir must be specified")
        }
        if (command == "") {
            throw GradleException("command must be specified")
        }

        shellCommand = ShellCommand(workingDir, command)
        shellCommand.start()

        if (shellCommand.failed) {
            val message = "command failed with exit code $exitValue"
            if (!ignoreExitValue) {
                throw GradleException(message)
            }
            logger.error(message)
        }
    }

    /**
     * Builds a custom value for the PATH variable.
     */
    private fun buildPath() {
        var path = systemPath
        logger.info("System.env.PATH: $systemPath")
        prePath?.let { pre: String ->
            path = "$pre:$path"
        }
        postPath?.let { post: String ->
            path = "$path:$post"
        }
        environment.put(PATH, path)
        logger.info("PATH: ${environment[PATH]}")
    }
}
