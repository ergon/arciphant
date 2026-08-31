import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
    `jvm-test-suite`
    `java-test-fixtures`
}

dependencies {
    implementation(lib("spring-context"))
    implementation(lib("jackson-module-kotlin"))
    testFixturesImplementation(lib("jackson-module-kotlin"))

    testFixturesApi(lib("junit-jupiter-api"))
    testRuntimeOnly(lib("junit-jupiter-engine"))
    testFixturesApi(lib("assertj-core"))
    testFixturesApi(lib("mockito-kotlin"))
    testFixturesApi(lib("spring-boot-starter-test"))
    // webflux is required to use WebTestClient
    testRuntimeOnly(lib("spring-boot-starter-webflux"))
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
