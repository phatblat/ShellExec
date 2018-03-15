package at.phatbl.shellexec

import at.phatbl.shellexec.logging.GradleLogOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.OutputStream

open class ShellExec: DefaultTask() {
    companion object {
        // Directories to be prepended to PATH
        private const val pathAdditions = "./bin:/usr/local/bin"
        private const val PATH = "PATH"
    }

    @Input
    var environment = mutableMapOf<String, Any>()

    @InputDirectory
    var workingDir: File = project.projectDir

    @Internal
    val standardOutput: OutputStream = GradleLogOutputStream(logger, LogLevel.LIFECYCLE)

    @Internal
    val errorOutput: OutputStream = GradleLogOutputStream(logger, LogLevel.ERROR)

//    @Internal
//    val standardInput: InputStream
//        get() = shellCommand.process.outputStream

    @Input
    var ignoreExitValue: Boolean = false

    @Internal
    var exitValue: Int = -999
        get() = shellCommand.exitValue

    private lateinit var shellCommand: ShellCommand

    /** Core storage of command line to be executed */
    @Input
    var command = ""

    /** Property containing a copy of the PATH environment variable. */
    @Input
    private var systemPath: String

    /** Value to be prepended to the PATH. */
    @Internal
    var prePath: String? = null
        set(value) {
            field = value
            buildPath()
        }

    /** Value to be appended to the PATH. */
    @Internal
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
        preExec()

        if (command == "") throw GradleException("command must not be empty")

        shellCommand = ShellCommand(baseDir = workingDir, command = command)
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

        postExec()
    }

    /** Hook for running logic before the exec task action runs. */
    open fun preExec() {
        logger.debug("No custom logic in preExec")
    }

    /** Hook for running logic immediately after the exec task action runs. Does not run on command failure. */
    open fun postExec() {
        logger.debug("No custom logic in postExec")
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
