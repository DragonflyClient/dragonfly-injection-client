plugins {
    java
    kotlin("jvm") version "1.4.20"
}

group = "net.dragonfly"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))

    compileOnly("org.javassist:javassist:3.21.0-GA")
    compileOnly("org.ow2.asm:asm:9.0")
    compileOnly("org.ow2.asm:asm-tree:9.0")
    compileOnly("org.ow2.asm:asm-util:9.0")

    compileOnly(project(":shared:agent"))
}

tasks {
    jar {
        archiveFileName.set("injection-hook-shared.jar")
    }
}