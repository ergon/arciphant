plugins {
    /**
     * Event though we do not use the Kotlin DSL to write the arciphant plugin, it is helpful to use the 'kotlin-dsl'
     * plugin instead of the kotlin-plugin itself, since it does some useful stuff, see:
     * https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
     */
    `kotlin-dsl`
}

dependencies {
    // since Kotlin plugin is applied from within the ArciphantCorePlugin, we need this dependency here
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("org.assertj:assertj-core:3.27.0")
}

gradlePlugin {
    plugins {
        create("artifact-dsl-plugin") {
            id = "ch.ergon.arciphant.dsl"
            implementationClass = "ch.ergon.arciphant.dsl.ArciphantDslPlugin"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
