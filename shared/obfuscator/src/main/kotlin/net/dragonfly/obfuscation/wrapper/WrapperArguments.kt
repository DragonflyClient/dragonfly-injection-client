package net.dragonfly.obfuscation.wrapper

import com.xenomachina.argparser.ArgParser
import java.io.File

class WrapperArguments(parser: ArgParser) {
    val reversedMappings by parser.storing(
        "-r", "--reversed",
        help = "File that holds the reversed mappings or to which they should be saved"
    ) { File(this) }

    val indexFile by parser.storing(
        "-m", "--mapping-index",
        help = "File that holds the mapping index"
    ) { File(this) }

    val inputJar by parser.storing(
        "-i", "--input-jar",
        help = "Input file for the deobfuscated jar"
    )

    val outputJar by parser.storing(
        "-o", "--output-jar",
        help = "Output file for the obfuscated jar"
    )

    val specialSource by parser.storing(
        "-j", "--special-source",
        help = "Jar file of the specialsource obfuscator by md_5"
    )

    val minecraftJar by parser.storing(
        "-c", "--minecraft-jar",
        help = "Clean jar file of the Minecraft version that is obfuscated"
    )
}