package at.phatbl.simple_exec

import org.gradle.api.Plugin
import org.gradle.api.Project

class SimpleExecPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        println("Applying SimpleExecPlugin")
    }
}
