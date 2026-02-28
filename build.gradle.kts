plugins {
    kotlin("jvm") version "1.5.30"
    `maven-publish`
}

allprojects {
    group = "com.example"
    version = "0.1.0"
}

subprojects {
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
        repositories {
            maven {
                url = uri("https://my.repository.url")
            }
        }
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}