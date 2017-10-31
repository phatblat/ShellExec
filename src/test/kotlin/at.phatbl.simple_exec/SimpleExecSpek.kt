package at.phatbl.simple_exec

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimpleExecSpec: Spek({
    describe("Simple Exec Task") {
        val project = ProjectBuilder.builder().build()
        val task: SimpleExec = project.tasks.create("exec",  SimpleExec::class.java)

        it("can be created") {
            assertNotNull(task)
        }

        it("can run a simple command") {
            task.command = "true"

            task.execute()
            while (task.state.executing || !task.state.executed) {
                Object().wait(10)
            }

            val result = task.execResult
            assertNotNull(result)
            result.assertNormalExitValue()
        }

        it("can run a failing command") {
            task.command = "false"
            task.setIgnoreExitValue(true)

            task.execute()
            synchronized(task.state) {
                while (task.state.executing || !task.state.executed) {
                    Object().wait(10)
                }
            }

            val result = task.execResult
            assertNotNull(result)
            assertEquals(1, result.exitValue)
        }
    }
})
