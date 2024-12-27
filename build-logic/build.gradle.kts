plugins {
    `kotlin-dsl` // this is a gradle core plugin to write plugins in the Kotlin DSL
}

dependencies {
    // since Kotlin plugin is applied from within the ModulithSettingsPlugin, we need this dependency here
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("org.assertj:assertj-core:3.27.0")
}

gradlePlugin {
    plugins {
        create("modulith-configuration-settings-plugin") {
            id = "ch.cbossi.gradle.modulith.modulith-configuration-settings-plugin"
            implementationClass = "ch.cbossi.gradle.modulith.ModulithConfigurationSettingsPlugin"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
