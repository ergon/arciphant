import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
    `java-test-fixtures`
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("org.assertj:assertj-core:3.27.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}