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
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
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
            attributes["Main-Class"] = "net.dragonfly.obfuscation.wrapper.SpecialSourceWrapper"
        }

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        archiveName = "obfuscation-api.jar"
    }
}