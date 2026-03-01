plugins {
    kotlin("jvm") version "2.0.21" apply false
}

allprojects {
    group = "com.github.petiosm"   // MUST match your GitHub username exactly
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}