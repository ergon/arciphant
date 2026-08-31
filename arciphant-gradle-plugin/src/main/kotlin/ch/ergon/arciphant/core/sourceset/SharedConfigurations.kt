package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.GradlePluginIds.JAVA_LIBRARY
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin.API_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME
import org.gradle.api.tasks.SourceSet

/**
 * Shared dependency configurations let build scripts declare a dependency once for all component source sets
 * of the same kind: the standard configurations ('api', 'implementation', 'compileOnly', 'runtimeOnly') reach
 * every production source set, the standard test configurations ('testImplementation', …) every test source
 * set, and the 'testFixtures*' configurations (created by Arciphant) every test fixtures source set.
 *
 * The 'api' configurations are only available if the java-library plugin is applied: 'api' is created by that
 * plugin, and Arciphant creates 'testFixturesApi' to match.
 */

private const val TEST_FIXTURES_API_CONFIGURATION_NAME = "testFixturesApi"
private const val TEST_FIXTURES_IMPLEMENTATION_CONFIGURATION_NAME = "testFixturesImplementation"
private const val TEST_FIXTURES_COMPILE_ONLY_CONFIGURATION_NAME = "testFixturesCompileOnly"
private const val TEST_FIXTURES_RUNTIME_ONLY_CONFIGURATION_NAME = "testFixturesRuntimeOnly"

internal fun Project.createSharedConfigurations() {
    testFixturesImplementationConfiguration()
    testFixturesCompileOnlyConfiguration()
    testFixturesRuntimeOnlyConfiguration()
    pluginManager.withPlugin(JAVA_LIBRARY) {
        testFixturesApiConfiguration()
    }
}

internal fun Project.extendSharedProductionConfigurations(sourceSet: SourceSet) {
    getConfiguration(sourceSet.implementationConfigurationName).extendsFrom(getConfiguration(IMPLEMENTATION_CONFIGURATION_NAME))
    getConfiguration(sourceSet.compileOnlyConfigurationName).extendsFrom(getConfiguration(COMPILE_ONLY_CONFIGURATION_NAME))
    getConfiguration(sourceSet.runtimeOnlyConfigurationName).extendsFrom(getConfiguration(RUNTIME_ONLY_CONFIGURATION_NAME))
    // 'api' is created by the java-library plugin. pluginManager.withPlugin fires before the plugin's
    // apply() has created the configuration, so wire lazily via the configuration container instead.
    configurations.matching { it.name == API_CONFIGURATION_NAME }.all {
        getConfiguration(sourceSet.apiConfigurationName).extendsFrom(this)
    }
}

internal fun Project.extendSharedTestConfigurations(sourceSet: SourceSet) {
    getConfiguration(sourceSet.implementationConfigurationName).extendsFrom(getConfiguration(TEST_IMPLEMENTATION_CONFIGURATION_NAME))
    getConfiguration(sourceSet.compileOnlyConfigurationName).extendsFrom(getConfiguration(TEST_COMPILE_ONLY_CONFIGURATION_NAME))
    getConfiguration(sourceSet.runtimeOnlyConfigurationName).extendsFrom(getConfiguration(TEST_RUNTIME_ONLY_CONFIGURATION_NAME))
}

internal fun Project.extendSharedTestFixturesConfigurations(sourceSet: SourceSet) {
    getConfiguration(sourceSet.implementationConfigurationName).extendsFrom(testFixturesImplementationConfiguration())
    getConfiguration(sourceSet.compileOnlyConfigurationName).extendsFrom(testFixturesCompileOnlyConfiguration())
    getConfiguration(sourceSet.runtimeOnlyConfigurationName).extendsFrom(testFixturesRuntimeOnlyConfiguration())
    pluginManager.withPlugin(JAVA_LIBRARY) {
        getConfiguration(sourceSet.apiConfigurationName).extendsFrom(testFixturesApiConfiguration())
    }
}

private fun Project.testFixturesApiConfiguration() = maybeCreateSharedConfiguration(
    name = TEST_FIXTURES_API_CONFIGURATION_NAME,
    description = "API dependencies of all test fixtures source sets.",
)

private fun Project.testFixturesImplementationConfiguration() = maybeCreateSharedConfiguration(
    name = TEST_FIXTURES_IMPLEMENTATION_CONFIGURATION_NAME,
    description = "Implementation dependencies of all test fixtures source sets.",
)

private fun Project.testFixturesCompileOnlyConfiguration() = maybeCreateSharedConfiguration(
    name = TEST_FIXTURES_COMPILE_ONLY_CONFIGURATION_NAME,
    description = "Compile-only dependencies of all test fixtures source sets.",
)

private fun Project.testFixturesRuntimeOnlyConfiguration() = maybeCreateSharedConfiguration(
    name = TEST_FIXTURES_RUNTIME_ONLY_CONFIGURATION_NAME,
    description = "Runtime-only dependencies of all test fixtures source sets.",
)

private fun Project.maybeCreateSharedConfiguration(name: String, description: String): Configuration =
    configurations.findByName(name) ?: configurations.create(name) {
        isCanBeConsumed = false
        isCanBeResolved = false
        isVisible = false
        this.description = description
    }
