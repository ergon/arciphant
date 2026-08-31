import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun lib(alias: String) = libs.findLibrary(alias).get().get()

tasks.named("bootJar") { enabled = false }
tasks.named("jar") { enabled = true }

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}

// Arciphant makes the component configurations extend the shared dependency configurations:
// 'implementation'/… reach all production source sets, 'testImplementation'/… all test source
// sets and 'testFixturesImplementation'/… (created by Arciphant) all test fixtures source sets.
// ('api' and 'testFixturesApi' would additionally be available with the java-library plugin.)
dependencies {
    // dependencies of every production component (analog of 'spring-component')
    "implementation"("org.springframework:spring-context")
    "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")

    // test source sets inherit these because they extend their component's test fixtures source set
    "testFixturesImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    "testFixturesImplementation"("org.junit.jupiter:junit-jupiter-api:5.11.3")
    "testFixturesImplementation"("org.assertj:assertj-core:3.27.0")
    "testFixturesImplementation"("org.mockito.kotlin:mockito-kotlin:5.4.0")
    "testFixturesImplementation"("org.springframework.boot:spring-boot-starter-test")

    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    // webflux is required to use WebTestClient
    "testRuntimeOnly"("org.springframework.boot:spring-boot-starter-webflux")

    // component-specific dependencies (analog of 'jooq-component' and 'spring-web-component' in the
    // PROJECT layout — components are source sets here, so plugins cannot be applied per component).
    // Every functional module has the 'db', 'webApi' and 'web' components; the 'filestore' component
    // only exists in some modules, so its dependency is declared in their build.gradle.kts instead.
    "dbImplementation"(lib("jooq"))
    "webImplementation"("org.springframework.boot:spring-boot-starter-web")
    "webApiImplementation"("org.springframework.boot:spring-boot-starter-web")
    "webTestFixturesImplementation"("org.springframework.boot:spring-boot-starter-web")
    "webApiTestFixturesImplementation"("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
