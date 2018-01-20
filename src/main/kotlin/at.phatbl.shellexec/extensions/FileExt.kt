package at.phatbl.shellexec.extensions

import java.io.File

/**
 * Extension to easily add lines to a file.
 */
fun File.writeLine(line: String) {
    printWriter().write(line)
}
