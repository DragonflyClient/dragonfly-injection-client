plugins {
    java
}

group = "net.dragonfly"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(fileTree("libraries"))
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
        }
    }
}