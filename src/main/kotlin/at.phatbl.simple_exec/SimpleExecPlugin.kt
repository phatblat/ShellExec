package at.phatbl.simple_exec

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin class which currently does nothing.
 *   apply plugin: 'simple-exec'
 */
class SimpleExecPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        println("Applying SimpleExecPlugin")
    }
}
