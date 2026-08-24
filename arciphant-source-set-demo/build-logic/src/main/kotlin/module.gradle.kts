import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
}

// resolved lazily: the version catalog extension does not exist yet when this plugin
// is applied from the root project's allprojects block
fun libs() = extensions.getByType<VersionCatalogsExtension>().named("libs")

// In the SOURCE_SET component layout, components are source sets inside the module project,
// so per-component convention plugins cannot be applied. Instead, this module-level plugin
// assigns dependencies to the component configurations by name. configurations.configureEach
// is lazy, so it also covers the configurations Arciphant creates later for each component.
configurations.configureEach {
    fun add(dependencyNotation: Any) {
        dependencies.add(project.dependencies.create(dependencyNotation))
    }

    // Arciphant creates the component configurations while the JVM plugin is being applied, which can
    // be before the version catalog extension is registered — defer the lookup to dependency resolution.
    fun addFromCatalog(dependencyNotation: () -> Any) {
        dependencies.addLater(provider { project.dependencies.create(dependencyNotation()) })
    }

    // dependencies of every production component (analog of 'spring-component')
    val isProductionImplementation = name == "implementation" || (
            name.endsWith("Implementation")
                    && name != "testImplementation"
                    && !name.endsWith("TestImplementation")
                    && !name.endsWith("TestFixturesImplementation")
            )
    if (isProductionImplementation) {
        add("org.springframework:spring-context")
        add("com.fasterxml.jackson.module:jackson-module-kotlin")
    }
    if (name.endsWith("TestFixturesImplementation")) {
        add("com.fasterxml.jackson.module:jackson-module-kotlin")
    }
    if (name.endsWith("TestFixturesApi")) {
        add("org.junit.jupiter:junit-jupiter-api:5.11.3")
        add("org.assertj:assertj-core:3.27.0")
        add("org.mockito.kotlin:mockito-kotlin:5.4.0")
        add("org.springframework.boot:spring-boot-starter-test")
    }
    if (name.endsWith("TestRuntimeOnly")) {
        add("org.junit.jupiter:junit-jupiter-engine:5.11.3")
        add("org.junit.platform:junit-platform-launcher")
        // webflux is required to use WebTestClient
        add("org.springframework.boot:spring-boot-starter-webflux")
    }

    // component-specific dependencies (analog of 'jooq-component', 'minio-component', 'spring-web-component')
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
