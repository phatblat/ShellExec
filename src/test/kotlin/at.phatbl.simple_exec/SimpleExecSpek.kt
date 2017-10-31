package at.phatbl.simple_exec

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
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

            val result = task.execResult
            assertNotNull(result)
            result.assertNormalExitValue()
        }
    }
})
