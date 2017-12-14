package at.phatbl.simple_exec

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.process.ExecResult
import org.gradle.process.ProcessForkOptions
import java.io.File
import java.io.InputStream
import java.io.OutputStream

open class SimpleExec: DefaultTask() { //, GradleExec<SimpleExec> {
    companion object {
        // Directories to be prepended to PATH
        private const val pathAdditions = "./bin:/usr/local/bin"
        private const val PATH = "PATH"
    }

    var commandLine: List<String> = ArrayList()
    var executable: String? = null
    var args: List<String> = ArrayList()
    var environment: MutableMap<String, Any> = mutableMapOf()
    var workingDir: File? = null
    var standardInput: InputStream? = null
    var standardOutput: OutputStream? = null
    var errorOutput: OutputStream? = null
    var ignoreExitValue: Boolean = false
    var execResult: ExecResult? = null

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

    /**
     * Builds a custom value for the PATH variable.
     */
    private fun buildPath() {
        var path = systemPath
        project.logger.info("System.env.PATH: $systemPath")
        prePath?.let { pre: String ->
            path = "$pre:$path"
        }
        postPath?.let { post: String ->
            path = "$path:$post"
        }
        environment.put(PATH, path)
        project.logger.info("PATH: ${environment[PATH]}")
    }
}
