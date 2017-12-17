package at.phatbl.simple_exec

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
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

    /** Core storage of command line to be executed */
    @Input
    var commandLine = mutableListOf<String>()

    /** Convenience property for managing the first element of commandLine */
    var executable: String?
        get() = commandLine.firstOrNull()
        set(value) {
            value?.let {
                commandLine.set(index = 0, element = value)
                return
            }
            commandLine.removeAt(index = 0)
        }

    /** Convenience property for managing the elements of commandLine after the first */
    var args: List<String>
        get() {
            if (commandLine.count() >= 2) {
                return commandLine.subList(fromIndex = 1, toIndex = commandLine.count() - 1)
            }
            return listOf()
        }
        set(value) {
            val exec = executable ?: "???"
            commandLine.clear()
            commandLine.set(index = 0, element = exec)
            commandLine.addAll(value)
        }

    var environment = mutableMapOf<String, Any>()
    var workingDir: File = project.projectDir

    val standardOutput: OutputStream = LogOutputStream(logger, LogLevel.INFO)
    val errorOutput: OutputStream = LogOutputStream(logger, LogLevel.ERROR)
//    val standardInput: InputStream
//        get() = shellCommand.process.outputStream

    var ignoreExitValue: Boolean = false
    var execResult: ExecResult? = null

    val exitValue: Int
        get() = shellCommand.exitValue

    private lateinit var shellCommand: ShellCommand

    /**
     * Convenience property for populating commandLine using a single script.
     * Given string has whitespace trimmed and is split on space before being passed to commandLine.
     */
    var command: String
        get() = commandLine.joinToString(" ")
        set(value) { commandLine = value.trim().split(" ").toMutableList() }

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
        if (commandLine.isEmpty()) {
            throw GradleException("command must be specified")
        }

        shellCommand = ShellCommand(workingDir, command)
        shellCommand.standardOutput = standardOutput
        shellCommand.errorOutput = errorOutput
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
