import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
    `jvm-test-suite`
    `java-test-fixtures`
}

dependencies {
    implementation("org.springframework:spring-context")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testFixturesImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testFixturesApi("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testFixturesApi("org.assertj:assertj-core:3.27.0")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    // webflux is required to use WebTestClient
    testRuntimeOnly("org.springframework.boot:spring-boot-starter-webflux")
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
