plugins {
    java
}

group = "net.dragonfly"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree("libraries"))
}