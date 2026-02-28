plugins {
    kotlin("jvm") version "2.3.0"
    `java-gradle-plugin`
    `maven-publish`
}

// Gradle 9 removed ProjectDependency.getDependencyProject(), so we cannot use
// implementation(project(":kotlin-plugin")) when publishing.
// Instead we reference the kotlin-plugin jar output directly via files() and
// declare an explicit task dependency so it is built first.
val kotlinPluginJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    kotlinPluginJar(project(":kotlin-plugin", configuration = "archives"))
    implementation(files(kotlinPluginJar))
}

// Make sure the kotlin-plugin jar is built before we compile the gradle-plugin
tasks.named("compileKotlin") {
    dependsOn(":kotlin-plugin:jar")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("ktObfuscate") {
            id                  = "io.github.nullij.ktobfuscate"
            implementationClass = "io.github.nullij.ktobfuscate.gradle.KtObfuscateGradlePlugin"
            displayName         = "KtObfuscate"
            description         = "Kotlin compiler plugin that obfuscates strings, method names, and fields in bytecode"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}