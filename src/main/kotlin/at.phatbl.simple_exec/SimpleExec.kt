package at.phatbl.simple_exec

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class SimpleExec: Exec() {
    companion object {
        // Directories to be prepended to PATH
        const val pathAdditions = "./bin:/usr/local/bin"
        const val PATH = "PATH"
    }

    /**
     * String of commands to be executed by Gradle, split on space before being passed to commandLine.
     */
    @Input
    protected var command: String = ""
        get() {
            field = commandLine.joinToString(" ")
            return field
        }
        set(value) {
            field = value
            commandLine = field.trim().split(" ")
        }

    init {
        environment(PATH, "$pathAdditions:${System.getenv(PATH)}")
        doFirst {
            project.logger.info("System.env.PATH: ${System.getenv(PATH)}")
            project.logger.info("Custom PATH: ${environment[PATH]}")
        }
    }
}
