import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

plugins {
    id("module")
}

tasks.named("bootJar") { enabled = false }
tasks.named("jar") { enabled = true }

dependencies {
    "implementation"(lib("spring-context"))
    "implementation"(lib("jackson-module-kotlin"))

    "testFixturesImplementation"(lib("jackson-module-kotlin"))
    "testFixturesImplementation"(lib("junit-jupiter-api"))
    "testFixturesImplementation"(lib("assertj-core"))
    "testFixturesImplementation"(lib("mockito-kotlin"))
    "testFixturesImplementation"(lib("spring-boot-starter-test"))

    "testRuntimeOnly"(lib("junit-jupiter-engine"))
    "testRuntimeOnly"(lib("junit-platform-launcher"))
    // webflux is required to use WebTestClient
    "testRuntimeOnly"(lib("spring-boot-starter-webflux"))

    "dbImplementation"(lib("jooq"))
    "webImplementation"(lib("spring-boot-starter-web"))
    "webApiImplementation"(lib("spring-boot-starter-web"))
    "webTestFixturesImplementation"(lib("spring-boot-starter-web"))
    "webApiTestFixturesImplementation"(lib("spring-boot-starter-web"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
