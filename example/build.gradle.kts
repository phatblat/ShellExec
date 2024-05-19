/*
 * build.gradle
 * ShellExecExample
 */

import at.phatbl.shellexec.ShellExec

buildscript {
    repositories.gradlePluginPortal()
    dependencies.classpath("at.phatbl:shellexec:+")
}

tasks {
    val helloWorld by registering(ShellExec::class) {
        group = "Example"
        command = "echo Hello World!"
    }

    val successfulCommand by registering(ShellExec::class) {
        group = "Example"
        command = "true"
    }

    val failingCommand by registering(ShellExec::class) {
        group = "Example"
        command = "false"
    }

    val emptyCommand by registering(ShellExec::class) {
        group = "Example"
        command = ""
    }

    val errorLogging by registering(ShellExec::class) {
        group = "Example"
        command = "echo This is an error >&2"
    }

    val customPath by registering(ShellExec::class) {
        group = "Example"
        prePath = "/Library/Java/JavaVirtualMachines/jdk-9.0.1.jdk/Contents/Home/bin"
        postPath = ""
        command = "env | grep PATH= && java -version"
    }

    val lolBoxFortune by registering(ShellExec::class) {
        group = "Example"
        command = "fortune | boxes --design parchment | lolcat"
    }

    /** [ShellExec] Extension used by customTask below. */
    open class CustomTask : ShellExec() {
        @Input
        var message: String = ""

        override fun preExec() {
            command = "echo $message"
        }
    }

    val customTask by registering(CustomTask::class) {
        group = "Example"
        message = "Custom task command set from preExec"
    }

    /** No-op task to prevent issues when IDEA refreshes project. */
    val testClasses by registering
}
