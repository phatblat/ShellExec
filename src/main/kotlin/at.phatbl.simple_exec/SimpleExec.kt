package at.phatbl.simple_exec

import at.phatbl.simple_exec.logging.GradleLogOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import java.io.File
//import java.io.InputStream
import java.io.OutputStream

open class SimpleExec : DefaultTask() {
    companion object {
        // Directories to be prepended to PATH
        private const val pathAdditions = "./bin:/usr/local/bin"
        private const val PATH = "PATH"
    }

    var environment = mutableMapOf<String, Any>()
    var workingDir: File = project.projectDir

    val standardOutput: OutputStream = GradleLogOutputStream(logger, LogLevel.LIFECYCLE)
    val errorOutput: OutputStream = GradleLogOutputStream(logger, LogLevel.LIFECYCLE)
//    val standardInput: InputStream
//        get() = shellCommand.process.outputStream

    var ignoreExitValue: Boolean = false
    var execResult: ExecResult? = null

    val exitValue: Int
        get() = shellCommand.exitValue

    private lateinit var shellCommand: ShellCommand

    /** Core storage of command line to be executed */
    @Input
    var command: String? = null

    /** Property containing a copy of the PATH environment variable. */
    @Input
    private var systemPath: String

    /** Value to be prepended to the PATH. */
//    @Input
    var prePath: String? = null
        set(value) {
            field = value
            buildPath()
        }

    /** Value to be appended to the PATH. */
//    @Input
    var postPath: String? = null
        set(value) {
            field = value
            buildPath()
        }

    init {
        systemPath = System.getenv(PATH)
    }

    @TaskAction
    fun exec() {
        val cmd = command ?: throw GradleException("command must be specified")

        shellCommand = ShellCommand(baseDir = workingDir, command = cmd)
        shellCommand.standardOutput = standardOutput
        shellCommand.errorOutput = errorOutput
        shellCommand.start()
//
//        logger.lifecycle(shellCommand.stdout)
//        logger.error(shellCommand.stderr)

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
