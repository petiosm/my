plugins {
    alias(libs.plugins.kotlin.jvm) apply false  // version comes from libs.versions.toml
}

allprojects {
    group = "com.github.petiosm"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}