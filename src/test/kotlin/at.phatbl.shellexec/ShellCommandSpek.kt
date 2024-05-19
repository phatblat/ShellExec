package at.phatbl.shellexec

import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

object ShellCommandSpek : Spek({
    describe("Shell Command") {
        var shellCommand: ShellCommand

        beforeEachTest {}

        it("can run a simple command") {
            shellCommand = ShellCommand(command = "true")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertFalse(shellCommand.failed)
            assertEquals(0, shellCommand.exitValue)
        }

        it("can run a failing command") {
            shellCommand = ShellCommand(command = "false")
            shellCommand.start()

            assertTrue(shellCommand.failed)
            assertFalse(shellCommand.succeeded)
            assertEquals(1, shellCommand.exitValue)
        }

        it("can generate standard output") {
            shellCommand = ShellCommand(command = "echo Hello World!")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("Hello World!\n", shellCommand.stdout)
            assertNull(shellCommand.standardOutput)
        }

        it("has empty but not null standard output") {
            shellCommand = ShellCommand(command = "echo -n")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("", shellCommand.stdout)
            assertNull(shellCommand.standardOutput)
        }

        it("can generate standard output to a stream") {
            shellCommand = ShellCommand(command = "echo Hello World!")
            val stream = ByteArrayOutputStream()
            shellCommand.standardOutput = stream

            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("Hello World!\n", String(stream.toByteArray()))
            assertNull(shellCommand.stdout)
        }

        it("can generate error output") {
            shellCommand = ShellCommand(command = "echo This is an error. >&2")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("This is an error.\n", shellCommand.stderr)
            assertNull(shellCommand.errorOutput)
        }

        it("has empty but not null error output") {
            shellCommand = ShellCommand(command = "echo -n >&2")
            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("", shellCommand.stderr)
            assertNull(shellCommand.errorOutput)
        }

        it("can generate error output to a stream") {
            shellCommand = ShellCommand(command = "echo This is an error. >&2")
            val stream = ByteArrayOutputStream()
            shellCommand.errorOutput = stream

            shellCommand.start()

            assertTrue(shellCommand.succeeded)
            assertEquals("This is an error.\n", String(stream.toByteArray()))
            assertNull(shellCommand.stderr)
        }

        it("can invoke a command with spaces in the path") {
            val fileName = "File with spaces in the name"
            val fileContents = "This is the file contents!"

            val temporaryFolder = TemporaryFolder()
            temporaryFolder.create()
            val baseDir = temporaryFolder.root
            val file = temporaryFolder.newFile(fileName)

            file.writeText(fileContents)

            shellCommand = ShellCommand(baseDir = baseDir, command = "cat '$fileName'")
            shellCommand.start()

            val stderr = shellCommand.stderr
            val stdout = shellCommand.stdout

            println(stderr)
            println(stdout)

            assertTrue(shellCommand.succeeded)
            assertEquals("", stderr)
            assertEquals(fileContents, stdout)

            temporaryFolder.delete()
        }

        it("can invoke a command with spaces in the current directory") {
            val dirName = "directory with spaces in the name"

            val temporaryFolder = TemporaryFolder()
            temporaryFolder.create()
            val baseDir = temporaryFolder.root
            val subDir = File(baseDir, dirName)
            subDir.mkdir()

            shellCommand = ShellCommand(baseDir = subDir, command = "true")
            shellCommand.start()

            val stderr = shellCommand.stderr
            val stdout = shellCommand.stdout

            println(stderr)
            println(stdout)

            assertTrue(shellCommand.succeeded)
            assertEquals("", stderr)

            temporaryFolder.delete()
        }

        it("can be cancelled early with a short timeout") {
            shellCommand = ShellCommand(command = "sleep 10")
            shellCommand.timeout = 1
            shellCommand.start()

            assertFalse(shellCommand.succeeded)
            assertEquals(-999, shellCommand.exitValue)
        }
    }
})
