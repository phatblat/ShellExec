package at.phatbl.simple_exec

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimpleExecSpec: Spek({
    describe("Simple Exec Task") {
        var project: Project = ProjectBuilder.builder().build()
        var task: SimpleExec = project.tasks.create("exec",  SimpleExec::class.java)

        beforeEachTest {
            project = ProjectBuilder.builder().build()
            task = project.tasks.create("exec",  SimpleExec::class.java)
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
