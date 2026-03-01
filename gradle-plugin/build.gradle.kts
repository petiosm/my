// this is gradle-plugin

plugins {
    kotlin("jvm")           // version inherited from root
    `java-gradle-plugin`
    `maven-publish`
}

val kotlinPluginJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    kotlinPluginJar(project(":kotlin-plugin", configuration = "archives"))
    implementation(files(kotlinPluginJar))
}

tasks.named("compileKotlin") {
    dependsOn(":kotlin-plugin:jar")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("ktObfuscate") {
            id                  = "com.github.petiosm.ktobfuscate"
            implementationClass = "io.github.nullij.ktobfuscate.gradle.KtObfuscateGradlePlugin"
            displayName         = "KtObfuscate"
            description         = "Kotlin compiler plugin that obfuscates strings, method names, and fields in bytecode"
        }
    }
}
