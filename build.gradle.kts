plugins {
    id("java-library")
    id("maven-publish")
}

allprojects {
    group = "com.github.petiosm"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        publications {
            register<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Multi-module project with Gradle plugin and runtime")
                    url.set("https://github.com/petiosm/my")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("petiosm")
                            name.set("Petiosm")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/petiosm/my.git")
                        developerConnection.set("scm:git:https://github.com/petiosm/my.git")
                        url.set("https://github.com/petiosm/my")
                    }
                }
            }
        }
    }
}