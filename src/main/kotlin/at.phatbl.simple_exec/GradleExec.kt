package at.phatbl.simple_exec

import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.ExecAction
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Interface for compatibility with the Gradle Exec task.
 */
interface GradleExec <T: Any> { //<T: AbstractExecTask>: ExecSpec {
    fun commandLine(vararg arguments: Any): T

    fun commandLine(args: Iterable<*>): T

    fun args(vararg args: Any): T

    fun args(args: Iterable<*>): T

    fun setArgs(arguments: List<String>): T

    fun setArgs(arguments: Iterable<*>): T

    @Optional
    @Input
    fun getArgs(): List<String>

    @Internal
    fun getCommandLine(): List<String>

    fun setCommandLine(args: List<String>)

    fun setCommandLine(args: Iterable<*>)

    fun setCommandLine(vararg args: Any)

    @Optional
    @Input
    fun getExecutable(): String

    fun setExecutable(executable: String)

    fun setExecutable(executable: Any)

    fun executable(executable: Any): T

    @Internal
    // TODO:LPTR Should be a content-less @InputDirectory
    fun getWorkingDir(): File

    fun setWorkingDir(dir: File)

    fun setWorkingDir(dir: Any)

    fun workingDir(dir: Any): T

    @Internal
    fun getEnvironment(): Map<String, Any>

    fun setEnvironment(environmentVariables: Map<String, *>)

    fun environment(name: String, value: Any): T

    fun environment(environmentVariables: Map<String, *>): T

    fun copyTo(target: ProcessForkOptions): T

    fun setStandardInput(inputStream: InputStream): T

    @Internal
    fun getStandardInput(): InputStream

    fun setStandardOutput(outputStream: OutputStream): T

    @Internal
    fun getStandardOutput(): OutputStream

    fun setErrorOutput(outputStream: OutputStream): T

    @Internal
    fun getErrorOutput(): OutputStream

    fun setIgnoreExitValue(ignoreExitValue: Boolean): T

    @Input
    fun isIgnoreExitValue(): Boolean

    /**
     * Returns the result for the command run by this task. Returns `null` if this task has not been executed yet.
     *
     * @return The result. Returns `null` if this task has not been executed yet.
     */
    @Internal
    fun getExecResult(): ExecResult
}
