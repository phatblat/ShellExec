package at.phatbl.shellexec.extensions

import org.gradle.api.Task

/** Execute all task actions. */
fun Task.executeActions() = actions.forEach { it.execute(this) }
