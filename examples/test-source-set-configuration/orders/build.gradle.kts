plugins {
    `java-library`
}

dependencies {
    // Arciphant's shared configurations: reaches the test source sets of all components
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
