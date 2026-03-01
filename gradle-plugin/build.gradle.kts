plugins {
    kotlin("jvm")
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

publishing {
    publications {
        // java-gradle-plugin auto-creates a "pluginMaven" publication for the jar
        // and a marker publication per plugin ID — both are needed for JitPack.
        // No extra configuration required; they inherit group/version from the root.
    }

    repositories {
        // JitPack builds in CI and reads from the local Maven repo,
        // so no extra repository block is needed here.
    }
}