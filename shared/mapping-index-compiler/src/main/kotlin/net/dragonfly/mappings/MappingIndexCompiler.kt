package net.dragonfly.mappings

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.hash.Hashing
import com.google.common.io.Files
import java.io.File
import java.net.URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MappingIndexCompiler(
    private val destinationDir: File,
    private val tempDir: File,
) {
    private val mappingsZip = "mappings.zip" inside tempDir
    private val srgZip = "srg.zip" inside tempDir
    private val checksumFile = "checksum.json" inside destinationDir
    private val configDir = "config" inside tempDir

    fun compile(index: MappingIndex) {
        println("== Compiling index $index ==")

        downloadIndexFiles(index)

        if (!searchExistingIndexes()) {
            setupDestinationDirectory()
            extractIndexFiles()

            if (!index.isModernVersion()) {
                combineSrgWithMappings(readLegacy(), "index.pack")
                combineSrgWithMappings(("joined.srg" inside tempDir).readLines(), "mappings.srg")
            } else {
                combineSrgWithMappings(readModern(), "index.pack")
            }
        }

        cleanTempDir()

        println("+ Done")
    }

    private fun readModern(): List<String> {
        println("> Reading mappings from modern file format")
        return ("joined.tsrg" inside configDir).readLines()
    }

    private fun readLegacy(): List<String> {
        println("> Converting mappings from legacy file format")
        return FormatConverter.convertLegacyToModern(("joined.srg" inside tempDir).readLines())
    }

    private fun combineSrgWithMappings(srgNames: List<String>, output: String) {
        println("> Merging mappings with srg names into $output")

        val fieldMappings = ("fields.csv" inside tempDir).parseCSV()
        val methodMappings = ("methods.csv" inside tempDir).parseCSV()

        val joined = srgNames.joinToString("\n")
            .replace(Regex("field_\\S+")) { fieldMappings[it.value] ?: it.value }
            .replace(Regex("func_\\S+")) { methodMappings[it.value] ?: it.value }

        (output inside destinationDir).writeText(joined)
    }

    private fun extractIndexFiles() {
        println("> Extracting index files")

        ZippingUtils.run {
            unzip(mappingsZip, tempDir)
            unzip(srgZip, tempDir)
        }

        // delete old zip files
        mappingsZip.delete()
        srgZip.delete()
    }

    private fun downloadIndexFiles(index: MappingIndex) {
        cleanTempDir(recreate = true)

        // download mappings and srg zips
        println("> Downloading files")
        makeMappingsUrl(index)
            .also { println("> Mappings: $it") }
            .readBytes().let { mappingsZip.writeBytes(it) }

        makeSrgUrl(index)
            .also { println("> Searge: $it") }
            .readBytes().let { srgZip.writeBytes(it) }
    }

    private fun cleanTempDir(recreate: Boolean = false) {
        if (tempDir.exists()) {
            println("> Cleaning temp directory")
            tempDir.deleteRecursively()
            if (recreate)
                tempDir.mkdirs()
        }
    }

    private fun setupDestinationDirectory() {
        println("> Setting up destination directory")

        destinationDir.deleteRecursively()

        val checksumObject = Main.jackson.createObjectNode()
            .put("srg", srgZip.checksum())
            .put("mappings", mappingsZip.checksum())

        destinationDir.mkdirs()
        checksumFile.writeText(checksumObject.toPrettyString())
    }

    private fun searchExistingIndexes(): Boolean {
        println("> Searching for existing indexes")

        // check if all mapping files exist
        if (!("index.pack" inside destinationDir).exists()) {
            println("> No fully complete indexes found")
            return false
        }

        println("> Found existing indexes")

        // search checksum file in destination directory
        if (!checksumFile.exists()) {
            println("> No checksum file found, cannot reuse indexes")
            return false
        }

        try {
            println("> Comparing index checksums with zip file checksums")

            val checksumObject = Main.jackson.readTree(checksumFile) as ObjectNode
            val expectedSrg = checksumObject.get("srg").textValue()
            val expectedMappings = checksumObject.get("mappings").textValue()

            if (expectedSrg != srgZip.checksum() || expectedMappings != mappingsZip.checksum()) {
                println("> Checksums don't match, have to recompile indexes")
                return false
            }

            println("> Checksums match, reusing indexes")
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    private fun makeMappingsUrl(index: MappingIndex): URL = index.run {
        if (isModernVersion()) {
            makeForgeMappingsUrl(index)
        } else {
            URL("http://export.mcpbot.bspk.rs/" +
                    "mcp_${channel}/" +
                    "${indexVersion}-${minecraftVersion}/" +
                    "mcp_${channel}-${indexVersion}-${minecraftVersion}.zip")
        }
    }

    private fun makeSrgUrl(index: MappingIndex) = index.run {
        if (isModernVersion()) {
            makeForgeSrgUrl(index)
        } else {
            URL("http://export.mcpbot.bspk.rs/" +
                    "mcp/" +
                    "${minecraftVersion}/" +
                    "mcp-${minecraftVersion}-srg.zip")
        }
    }

    // http://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_snapshot/20201028-1.16.3/mcp_snapshot-20201028-1.16.3.zip
    // http://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_snapshot/20201210-1.15.1/mcp_snapshot-20201210-1.15.1.zip
    private fun makeForgeSrgUrl(index: MappingIndex): URL = index.run {
        var targetUrl = "https://files.minecraftforge.net/" +
                "maven/de/oceanlabs/mcp/mcp_config/" +
                "index_$minecraftVersion.html"

        if (Main.programArguments.version != minecraftVersion) {
            // if using outdated mcp mappings, try to find newer searge mappings
            val latestForgeUrl = "https://files.minecraftforge.net/" +
                    "maven/de/oceanlabs/mcp/mcp_config/" +
                    "index_${Main.programArguments.version}.html"

            if (URL(latestForgeUrl).exists()) {
                println("> Found newer searge mappings on Forge Maven")
                targetUrl = latestForgeUrl
            }
        }

        val doc: Document = Jsoup.connect(targetUrl).get()
        val link = doc.getElementsByClass("link")[0].child(0).attr("href")
        URL("https://files.minecraftforge.net$link")
    }

    private fun makeForgeMappingsUrl(index: MappingIndex): URL = index.run {
        URL("http://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_snapshot/" +
                "$indexVersion-$minecraftVersion/" +
                "mcp_$channel-$indexVersion-$minecraftVersion.zip")
    }

    @Suppress("UnstableApiUsage")
    private fun File.checksum() = Files.asByteSource(this).hash(Hashing.sha1()).toString()

    private fun File.parseCSV() = readLines().drop(1).map { it.split(",") }.associate { it[0] to it[1] }

    private fun URL.exists(): Boolean = try {
        readBytes()
        true
    } catch (e: Throwable) {
        false
    }

    private infix fun String.inside(directory: File) = File(directory.also { it.mkdirs() }, this)
}