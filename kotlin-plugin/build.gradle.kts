plugins {
    kotlin("jvm") version "2.3.0"
    `maven-publish`
}

dependencies {
    compileOnly(libs.kotlinc.embeddable)
    // compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.0")
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinc.embeddable)
    // testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.0")
}

kotlin {
    jvmToolchain(17)
}

// Fat jar so the compiler can load it as a plugin without classpath headaches
tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}