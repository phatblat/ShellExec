package at.phatbl.shellexec

import at.phatbl.shellexec.extensions.executeActions
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShellExecSpek : Spek({
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
            task.executeActions()

            assertEquals(task.exitValue, 0)
        }

        it("can run a failing command") {
            task.command = "false"
            task.ignoreExitValue = true
            task.executeActions()

            assertEquals(task.exitValue, 1)
        }
    }
})
