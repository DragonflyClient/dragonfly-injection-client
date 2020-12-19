group = "net.dragonfly"
version = "1.0-SNAPSHOT"

val editionProjects = project.subprojects.filter { it.name.contains(".") }
    .sortedWith(compareVersions())
val editions = editionProjects.map { Edition(it.name.extractVersion(), it.name.extractCodename()) }

println("\n-- Loading Dragonfly Editions --")
editions.forEach { (version, codename) ->
    println("> ${codename?.capitalize() ?: "undefined"} -> $version")
}
println("--------------------------------\n")

tasks {
    editions.forEach { edition ->
        val (version, codename) = edition

        create<Exec>("mappings-$version") {
            onlyIf {
                !File("mappings/$version/index.pack").exists()
            }
            group = "$version ($codename)"
            dependsOn(":shared:mapping-index-compiler:binJar")
            workingDir = projectDir
            commandLine("cmd", "/c", "java", "-jar", "bin/mapping-index-compiler.jar")
            args("--version", version)
            args("--destination-dir", "mappings/$version")
            args("--temp-dir", "temp/")
        }

        create<Exec>("obfuscate-$version") {
            onlyIf {
                !project(":$version:injection-hook").tasks.getByName("jar").state.upToDate ||
                        !project(":shared:obfuscator").tasks.getByName("binJar").state.upToDate
            }

            group = "$version ($codename)"
            dependsOn("mappings-$version", ":$version:injection-hook:jar", ":shared:obfuscator:binJar")

            workingDir = projectDir
            commandLine("cmd", "/c", "java", "-jar", "bin/obfuscator.jar")
            args("--reversed", "mappings/$version/reversed.srg")
            args("--mapping-index", "mappings/$version/index.pack")
            args("--minecraft-jar", "$version/minecraft/build/libs/minecraft-1.0-SNAPSHOT.jar")
            args("--input-jar", "$version/injection-hook/build/libs/injection-hook-$version.jar")
            args("--output-jar", "$version/injection-hook/build/libs/injection-hook-$version-obfuscated.jar")
            args("--special-source", "bin/specialsource.jar")
        }

        create<Copy>("components-$version") {
            group = "$version ($codename)"
            dependsOn("obfuscate-$version", ":shared:agent:jar", ":shared:injection-hook:jar")
            from(
                "$version/injection-hook/build/libs/injection-hook-$version-obfuscated.jar",
                "shared/agent/build/libs",
                "shared/injection-hook/build/libs"
            )
            into("components/$version")
            rename {
                when(it) {
                    "injection-hook-$version-obfuscated.jar" -> "injection-hook-$version.jar"
                    else -> it
                }
            }
        }
    }

    create<Copy>("deploy") {
        editions.forEach { (version, _) ->
            dependsOn("components-$version")
            from("components/$version/")
        }

        into("${System.getenv("APPDATA")}/.minecraft/dragonfly/injection")
    }

    create<Exec>("launch") {
        doFirst {
            ProcessBuilder()
                .command("taskkill", "/F", "/IM", "electron.exe")
                .start()
        }

        workingDir = file("D:\\Inception Cloud\\Workspace\\Dragonfly\\dragonfly-launcher")
        commandLine("cmd", "/c", "npm", "start")
    }

    create("deployAndLaunch") {
        dependsOn("deploy", "launch")
    }
}

fun String.extractCodename() = split("-").takeIf { it.size == 2 }?.get(1)
fun String.extractVersion() = split("-")[0]
fun String.extractVersionPart(index: Int) = split(".").takeIf { it.size >= index + 1 }?.get(index)?.toInt() ?: 0
fun compareVersions() = object : Comparator<Project> {
    override fun compare(o1: Project?, o2: Project?): Int {
        if (o1 == null && o2 == null) return 0
        if (o1 == null) return -1
        if (o2 == null) return 1

        val v1 = o1.name.extractVersion()
        val v2 = o2.name.extractVersion()

        for (i in 0..2) {
            val comp = v1.extractVersionPart(i).compareTo(v2.extractVersionPart(i))
            if (comp != 0) return comp
        }

        return 0
    }
}

data class Edition(val version: String, val codename: String?)