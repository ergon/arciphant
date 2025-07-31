plugins {
    /**
     * Event though we do not use the Kotlin DSL to write the arciphant plugin, it is helpful to use the 'kotlin-dsl'
     * plugin instead of the kotlin-plugin itself, since it does some useful stuff, see:
     * https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
     */
    `kotlin-dsl`
    /**
     * Required to publish plugin to gradle plugin portal
     */
    id("com.gradle.plugin-publish") version "1.3.1"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("org.assertj:assertj-core:3.27.0")
    // required for testing purposes only. In a real project, this plugin should be provided by the project itself
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
}

group = "ch.ergon.arciphant"
version = "0.1.1"

gradlePlugin {
    website = "https://github.com/ergon/arciphant"
    vcsUrl = "https://github.com/ergon/arciphant"

    plugins {
        create("arciphant") {
            id = "ch.ergon.arciphant"
            implementationClass = "ch.ergon.arciphant.core.ArciphantPlugin"

            displayName = "Arciphant"
            description =
                "Arciphant is a Gradle plugin that allows to specify the module structure of complex software project declaratively using a simple DSL."
            tags = listOf("architecture", "clean-architecture", "dependencies", "dependency-management", "dependency-manager")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
