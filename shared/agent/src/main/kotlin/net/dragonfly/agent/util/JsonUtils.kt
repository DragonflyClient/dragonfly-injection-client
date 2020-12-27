package net.dragonfly.agent.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Merges the JSON content of the [updateObject] into the [mainObject] and returns the modified [mainObject].
 * This function considers nested object and merges them recursively, while array nodes are simply overwritten.
 */
fun mergeJson(mainObject: ObjectNode, updateObject: ObjectNode): ObjectNode = mainObject.apply {
    updateObject.fieldNames().forEach { fieldName ->
        val mainNode = mainObject.get(fieldName)
        val updateNode = updateObject.get(fieldName)

        if (mainNode != null && mainNode is ObjectNode && updateNode is ObjectNode) {
            mergeJson(mainNode, updateNode)
        } else {
            mainObject.set<ObjectNode>(fieldName, updateNode)
        }
    }
}

/**
 * Returns the node casted to an [ObjectNode].
 */
fun JsonNode.asObject() = this as ObjectNode