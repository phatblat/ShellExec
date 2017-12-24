package at.phatbl.simple_exec

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShellCommandSpec : Spek({
    describe("Shell Command") {
        var shellCommand: ShellCommand
        beforeEachTest {
        }
        it("can run a simple command") {
            shellCommand = ShellCommand(baseDir = File("."), command = "true")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertFalse(shellCommand.failed)
            assertEquals(0, shellCommand.exitValue)
        }
        it("can run a failing command") {
            shellCommand = ShellCommand(baseDir = File("."), command = "false")
            shellCommand.start()

            assertTrue(shellCommand.failed)
            assertFalse(shellCommand.succeeded)
            assertEquals(1, shellCommand.exitValue)
        }
        it("can generate standard output") {
            shellCommand = ShellCommand(baseDir = File("."), command = "echo Hello World!")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("Hello World!\n", shellCommand.stdout)
        }
        it("can generate error output") {
            shellCommand = ShellCommand(baseDir = File("."), command = "echo This is an error. >&2")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("This is an error.\n", shellCommand.stderr)
        }
    }
})
