plugins { 
    `java-gradle-plugin` 
}

repositories { 
    mavenCentral() 
}

gradlePlugin { 
    plugins { 
        create("myPlugin") { 
            id = "com.example.myplugin" 
            implementationClass = "com.example.MyPlugin" 
        } 
    } 
} 

publishing { 
    publications { 
        create<MavenPublication>("pluginMaven") { 
            from(components["java"]) 
            groupId = "com.example" 
            artifactId = "myplugin" 
            version = "1.0.0" 
        } 
    } 
} 
