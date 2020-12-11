plugins {
    kotlin("jvm") version "1.4.20"
}

group = "net.dragonfly"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.3")
    implementation("com.google.guava:guava:30.0-jre")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("com.google.guava:guava:30.0-jre")
    implementation("org.jsoup:jsoup:1.13.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    getByName<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = "net.dragonfly.mappings.Main"
        }

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        archiveFileName.set("mapping-index-compiler.jar")
    }
}