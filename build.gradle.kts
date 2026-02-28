plugins {
    kotlin("jvm") version "2.3.0" apply false
}

allprojects {
    group = "com.github.nullij"  // JitPack requires com.github.<your-github-username>
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}