package at.phatbl.shellexec

import at.phatbl.shellexec.extensions.writeLine
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TempFolderUtil {
    @Rule @JvmField val folder = TemporaryFolder()
}

class ShellCommandSpek : Spek({
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

        it("can invoke a command with spaces in the path") {
            val fileName = "File with spaces in the name"
            val fileContents = "This is the file contents!"

            // JUnit TemporaryFOlder
            val util = TempFolderUtil()
            util.folder.create()
            val baseDir = util.folder.root
            val file = util.folder.newFile(fileName)

            file.writeText(fileContents)

            shellCommand = ShellCommand(baseDir = baseDir, command = "cat '$fileName'")
            shellCommand.start()

            val stderr = shellCommand.stderr
            val stdout = shellCommand.stdout

            println(stderr)
            println(stdout)

            assertTrue(shellCommand.succeeded)
            assertEquals("", stderr)
            assertEquals("$fileContents\n", stdout)
        }
    }
})
