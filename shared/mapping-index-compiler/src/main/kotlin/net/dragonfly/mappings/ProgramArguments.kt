package net.dragonfly.mappings

import com.xenomachina.argparser.ArgParser
import java.io.File

class ProgramArguments(parser: ArgParser) {
    val version by parser.storing(
        "-v", "--version",
        help = "Minecraft version for which the mappings should be provided"
    )

    val destinationDir by parser.storing(
        "-d", "--destination-dir",
        help = "directory in which the mappings are stored"
    ) { File(this) }

    val tempDir by parser.storing(
        "-t", "--temp-dir",
        help = "directory which is used for storing temporary files"
    ) { File(this) }
}