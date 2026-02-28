plugins {
    kotlin("jvm") version "2.3.0" apply false
}

allprojects {
    group = "io.github.nullij.ktobfuscate"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}