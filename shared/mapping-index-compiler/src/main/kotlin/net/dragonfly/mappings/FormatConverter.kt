package net.dragonfly.mappings

object FormatConverter {

    fun convertLegacyToModern(input: List<String>): List<String> {
        val lines = input.sorted()
        val classIndexes = mutableMapOf<String, Int>()
        val converted = mutableListOf<String>()

        fun append(toClass: String, line: String) {
            val position = classIndexes[toClass]!!
            converted.add(position, "\t$line")
            classIndexes.replaceAll { name, index ->
                if (name > toClass || name == toClass) (index + 1)
                else index
            }
        }

        for (line in lines) {
            when {
                line.startsWith("CL: ") -> {
                    val split = line.removePrefix("CL: ").split(" ")
                    val obf = split[0]
                    val srg = split[1]

                    converted.add("$obf $srg")
                    classIndexes[obf] = converted.size
                }
                line.startsWith("FD: ") -> {
                    val split = line.removePrefix("FD: ").split(" ")

                    val obf = split[0].split("/")
                    val obfClass = obf.dropLast(1).joinToString("/")
                    val obfField = obf.last()

                    val srg = split[1].split("/")
                    val srgField = srg.last()

                    append(obfClass, "$obfField $srgField")
                }
                line.startsWith("MD: ") -> {
                    val split = line.removePrefix("MD: ").split(" ")

                    val obf = split[0].split("/")
                    val obfClass = obf.dropLast(1).joinToString("/")
                    val obfMethod = obf.last()
                    val obfDesc = split[1]

                    val srg = split[2].split("/")
                    val srgMethod = srg.last()

                    append(obfClass, "$obfMethod $obfDesc $srgMethod")
                }
            }
        }

        return converted
    }
}