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
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")

    implementation("org.javassist:javassist:3.21.0-GA")
    implementation("org.ow2.asm:asm:9.0")
    implementation("org.ow2.asm:asm-tree:9.0")
    implementation("org.ow2.asm:asm-util:9.0")

    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation(project(":shared:obfuscator"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        manifest {
            attributes["Premain-Class"] = "net.dragonfly.agent.main.AgentMain"
            attributes["Agent-Class"] = "net.dragonfly.agent.main.AgentMain"
            attributes["Can-Redefine-Classes"] = "true"
            attributes["Can-Retransform-Classes"] = "true"
        }

        archiveName = "shared-agent.jar"
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}