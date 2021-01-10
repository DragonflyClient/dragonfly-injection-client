plugins {
    java
    kotlin("jvm") version "1.4.20"
}

group = "net.dragonfly"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))

    compileOnly("org.javassist:javassist:3.21.0-GA")
    compileOnly("org.ow2.asm:asm:9.0")
    compileOnly("org.ow2.asm:asm-tree:9.0")
    compileOnly("org.ow2.asm:asm-util:9.0")

    compileOnly("org.koin:koin-core:2.2.0")

    compileOnly(project(":shared:agent"))
    compileOnly(project(":shared:obfuscator"))
    compileOnly(project(":shared:dragonfly-core"))
    compileOnly(project(":1.8.8-octane:minecraft"))
    compileOnly(fileTree("../minecraft/libraries/"))
}

tasks {
    jar {
        archiveFileName.set("injection-hook-1.8.8.jar")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}