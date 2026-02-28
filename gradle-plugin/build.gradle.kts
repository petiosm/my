plugins {
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.example"
            artifactId = "my-plugin"
            version = "1.0.0"

            from(components["java"])
        }
    }
}