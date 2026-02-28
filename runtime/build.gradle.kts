plugins {
    `java-library`
}

task test(type: Test) {
    useJUnitPlatform()
}