plugins {
    kotlin("jvm") version "2.3.0" apply false  // ✅ must match libs.versions.toml
}

allprojects {
    group = "com.github.petiosm"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}