package at.phatbl.shellexec

import at.phatbl.shellexec.extensions.executeActions
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShellExecSubclassSpek : Spek({
    describe("Shell Exec Task") {
        lateinit var project: Project // = ProjectBuilder.builder().build()

        beforeEachTest {
            project = ProjectBuilder.builder().build()
        }

        it("calls preExec") {
            val task: PreExec = project.tasks.create("exec", PreExec::class.java)

            task.command = "true"
            task.executeActions()

            assertEquals(task.exitValue, 0)
            assertTrue { task.methodCalled }
        }

        it("calls postExec") {
            val task: PostExec = project.tasks.create("exec", PostExec::class.java)

            task.command = "true"
            task.executeActions()

            assertEquals(task.exitValue, 0)
            assertTrue { task.methodCalled }
        }

        it("calls preExec and postExec") {
            val task: PreAndPostExec = project.tasks.create("exec", PreAndPostExec::class.java)

            task.command = "true"
            task.executeActions()

            assertEquals(task.exitValue, 0)
            assertTrue { task.preExecCalled && task.postExecCalled }
        }
    }
})

open class PreExec : ShellExec() {
    var methodCalled = false

    override fun preExec() {
        methodCalled = true
    }
}

open class PostExec : ShellExec() {
    var methodCalled = false

    override fun postExec() {
        methodCalled = true
    }
}

open class PreAndPostExec : ShellExec() {
    var preExecCalled = false
    var postExecCalled = false

    override fun preExec() {
        preExecCalled = true
    }

    override fun postExec() {
        postExecCalled = true
    }
}
