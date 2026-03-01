plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

// Attach sources jar so JitPack consumers get IDE navigation
java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}