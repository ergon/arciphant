import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
}

// resolved lazily: the version catalog extension does not exist yet when this plugin
// is applied from the root project's subprojects block
fun libs() = extensions.getByType<VersionCatalogsExtension>().named("libs")

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
}

// component-specific dependencies (analog of 'jooq-component', 'minio-component', 'spring-web-component').
// In the SOURCE_SET component layout, components are source sets inside the module project, so per-component
// convention plugins cannot be applied. Instead, dependencies are assigned to the component configurations by
// name. configurations.configureEach is lazy, so it also covers the configurations Arciphant creates later.
configurations.configureEach {
    fun add(dependencyNotation: Any) {
        dependencies.add(project.dependencies.create(dependencyNotation))
    }

    // Arciphant creates the component configurations while the JVM plugin is being applied, which can
    // be before the version catalog extension is registered — defer the lookup to dependency resolution.
    fun addFromCatalog(dependencyNotation: () -> Any) {
        dependencies.addLater(provider { project.dependencies.create(dependencyNotation()) })
    }

    when (name) {
        "dbImplementation" -> addFromCatalog { libs().findLibrary("jooq").get().get() }
        "filestoreImplementation" -> addFromCatalog { libs().findLibrary("minio").get().get() }
        "webImplementation", "webApiImplementation",
        "webTestFixturesImplementation", "webApiTestFixturesImplementation",
            -> add("org.springframework.boot:spring-boot-starter-web")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
