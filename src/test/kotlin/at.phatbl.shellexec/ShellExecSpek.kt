package at.phatbl.shellexec

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShellExecSpec: Spek({
    describe("Shell Exec Task") {
        var project: Project = ProjectBuilder.builder().build()
        var task: ShellExec = project.tasks.create("exec", ShellExec::class.java)

        beforeEachTest {
            project = ProjectBuilder.builder().build()
            task = project.tasks.create("exec", ShellExec::class.java)
        }

        it("can be created") {
            assertNotNull(task)
        }

        xit("can run a simple command") {
            task.command = "true"
            task.execute()

            val result = task.execResult
            assertNotNull(result)
            result?.assertNormalExitValue()
        }

        xit("can run a failing command") {
            task.command = "false"
            task.ignoreExitValue = true
            task.execute()

            val result = task.execResult
            assertNotNull(result)
            assertEquals(1, result?.exitValue)
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
