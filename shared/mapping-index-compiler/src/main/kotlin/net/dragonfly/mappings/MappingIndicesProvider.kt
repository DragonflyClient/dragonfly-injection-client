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
    }

    fun get(): List<MappingIndex> {
        fetchIfAbsent()
        return mappings
    }

    fun get(minecraftVersion: String): MappingIndex? {
        fetchIfAbsent()

        val indexesForVersion = mappings.filter { it.minecraftVersion == minecraftVersion }
            .takeUnless { it.isEmpty() } ?: return null
        val preferredIndexes = indexesForVersion.filter { it.channel == "stable" }
            .takeUnless { it.isEmpty() } ?: indexesForVersion

        return preferredIndexes.maxByOrNull { it.indexVersion }
    }

    private fun fetchIfAbsent() {
        if (mappings.isEmpty()) {
            fetch()
        }
    }
}