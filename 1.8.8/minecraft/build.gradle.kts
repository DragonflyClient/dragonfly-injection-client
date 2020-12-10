plugins {
    java
}

group = "net.dragonfly"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(fileTree("libraries"))
}