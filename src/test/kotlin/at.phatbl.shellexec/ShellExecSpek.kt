package at.phatbl.shellexec

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShellExecSpek: Spek({
    describe("Shell Exec Task") {
        lateinit var project: Project
        lateinit var task: ShellExec

        beforeEachTest {
            project = ProjectBuilder.builder().build()
            task = project.tasks.create("exec", ShellExec::class.java)
        }

        it("can be created") {
            assertNotNull(task)
        }

        it("can run a simple command") {
            task.command = "true"
            task.execute()

            assertEquals(task.exitValue, 0)
        }

        it("can run a failing command") {
            task.command = "false"
            task.ignoreExitValue = true
            task.execute()

            assertEquals(task.exitValue, 1)
        }
    }
})
