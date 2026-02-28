plugins { 
    `java-library` 
}

java { 
    // Specify the version of Java you want to use
    toolchain { 
        languageVersion.set(JavaLanguageVersion.of(11)) 
    }
}

repositories { 
    mavenCentral() 
}

dependencies { 
    api("org.jetbrains.kotlin:kotlin-stdlib") 
    // Add your dependencies here 
}