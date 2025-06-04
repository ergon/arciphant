plugins {
    /**
     * Event though we do not use the Kotlin DSL to write the arciphant plugin, it is helpful to use the 'kotlin-dsl'
     * plugin instead of the kotlin-plugin itself, since it does some useful stuff, see:
     * https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
     */
    `kotlin-dsl`
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("org.assertj:assertj-core:3.27.0")
    // required for testing purposes only. In a real project, this plugin should be provided by the project itself
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
}

gradlePlugin {
    plugins {
        create("arciphant-core-plugin") {
            id = "ch.ergon.arciphant.core"
            implementationClass = "ch.ergon.arciphant.core.ArciphantCorePlugin"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
