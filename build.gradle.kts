group = "net.dragonfly"
version = "1.0-SNAPSHOT"

val minecraftVersionProjects = project.subprojects.filter { it.name.contains(".") }
val minecraftVersions = minecraftVersionProjects.map { it.name }

tasks {
    minecraftVersionProjects.forEach { versionProject ->
        val version = versionProject.name
        create<Exec>("mappings-$version") {
            group = version
            dependsOn(":shared:mapping-index-compiler:binJar")
            workingDir = projectDir
            commandLine("cmd", "/c", "java", "-jar", "bin/mapping-index-compiler.jar")
            args("--version", version)
            args("--destination-dir", "mappings/$version")
            args("--temp-dir", "temp/")
        }

        create<Exec>("obfuscate-$version") {
            group = version
            dependsOn("mappings-$version", ":$version:injection-hook:jar", ":shared:obfuscator:binJar")
            workingDir = projectDir
            commandLine("cmd", "/c", "java", "-jar", "bin/obfuscator.jar")
            args("--searge", "mappings/$version/mappings.srg")
            args("--mapping-index", "mappings/$version/index.pack")
            args("--input-jar", "$version/injection-hook/build/libs/injection-hook-$version.jar")
            args("--output-jar", "$version/injection-hook/build/libs/injection-hook-$version-obfuscated.jar")
            args("--special-source", "bin/specialsource.jar")
        }

        create<Copy>("components-$version") {
            group = version
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
        minecraftVersions.forEach { version ->
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