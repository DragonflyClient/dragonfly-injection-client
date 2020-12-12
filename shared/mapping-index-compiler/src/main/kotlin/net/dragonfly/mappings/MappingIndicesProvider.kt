package net.dragonfly.mappings

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.net.URL

class MappingIndicesProvider {
    private val url = "http://export.mcpbot.bspk.rs/versions.json"
    private val mappings = mutableListOf<MappingIndex>()

    private fun fetch() {
        mappings.clear()
        val text = URL(url).readText()
        val objectNode = Main.jackson.readTree(text) as ObjectNode

        objectNode.fieldNames().forEachRemaining { minecraftVersion ->

            val minecraftVersionObject = objectNode.get(minecraftVersion) as ObjectNode
            minecraftVersionObject.fieldNames().forEachRemaining { channel ->

                val channelObject = minecraftVersionObject.get(channel) as ArrayNode
                channelObject.forEach { node ->

                    val indexVersion = node.intValue()
                    mappings.add(MappingIndex(minecraftVersion, channel, indexVersion))
                }
            }
        }

        mappings.add(MappingIndex("1.16.3", "snapshot", 20201028))
    }

    fun get(): List<MappingIndex> {
        fetchIfAbsent()
        return mappings
    }

    fun get(minecraftVersion: String): MappingIndex? {
        fetchIfAbsent()

        val indexesForVersion = mappings.filter { it.minecraftVersion == minecraftVersion }
            .takeUnless { it.isEmpty() }
            ?: getIndexesForMajorVersion(minecraftVersion)
            ?: return null

        val preferredIndexes = indexesForVersion.filter { it.channel == "stable" }
            .takeUnless { it.isEmpty() }
            ?: indexesForVersion

        return preferredIndexes.maxByOrNull { it.indexVersion }
    }

    private fun getIndexesForMajorVersion(minecraftVersion: String): List<MappingIndex>? {
        val split = minecraftVersion.split(".")
        if (split.size == 2) return null

        println("> Have to use indices from major version ${split[0]}.${split[1]}")

        return mappings.filter {
            val itSplit = it.minecraftVersion.split(".")
            itSplit[0] == split[0] && itSplit[1] == split[1]
        }.takeUnless { it.isEmpty() }
    }

    private fun fetchIfAbsent() {
        if (mappings.isEmpty()) {
            fetch()
        }
    }
}