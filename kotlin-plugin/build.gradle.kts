// this is kotlin-plugin

plugins {
    kotlin("jvm")           // version inherited from root
    `maven-publish`
}

dependencies {
    compileOnly(libs.kotlinc.embeddable)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinc.embeddable)
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
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
            groupId = project.group.toString()
            artifactId = "kotlin-plugin"
            version = project.version.toString()
        }
    }
}