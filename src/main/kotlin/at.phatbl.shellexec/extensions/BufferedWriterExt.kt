package at.phatbl.shellexec.extensions

import java.io.BufferedWriter

/**
 * Extension to easily add lines to a file.
 */
fun BufferedWriter.writeLine(line: String) {
    this.write(line)
    this.newLine()
}
