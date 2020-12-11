package net.dragonfly.mappings

data class MappingIndex(
    val minecraftVersion: String,
    val channel: String,
    val indexVersion: Int
) {
    override fun toString() = "$minecraftVersion (#$indexVersion - $channel)"

    fun isModernVersion() = minecraftVersion.split(".").let {
        it[1].toInt() > 12 || (it.size > 2 && it[1].toInt() == 12 && it[2].toInt() >= 2)
    }
}