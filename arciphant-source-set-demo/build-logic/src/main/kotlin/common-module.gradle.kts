import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

plugins {
    id("module")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

tasks.named("bootJar") { enabled = false }
tasks.named("jar") { enabled = true }

dependencies {
    "implementation"("org.springframework:spring-context")
    "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")

    "testFixturesImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    "testFixturesImplementation"("org.junit.jupiter:junit-jupiter-api:5.11.3")
    "testFixturesImplementation"("org.assertj:assertj-core:3.27.0")
    "testFixturesImplementation"("org.mockito.kotlin:mockito-kotlin:5.4.0")
    "testFixturesImplementation"("org.springframework.boot:spring-boot-starter-test")

    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    // webflux is required to use WebTestClient
    "testRuntimeOnly"("org.springframework.boot:spring-boot-starter-webflux")

    "dbImplementation"(libs.findLibrary("jooq").get())
    "webImplementation"("org.springframework.boot:spring-boot-starter-web")
    "webApiImplementation"("org.springframework.boot:spring-boot-starter-web")
    "webTestFixturesImplementation"("org.springframework.boot:spring-boot-starter-web")
    "webApiTestFixturesImplementation"("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
