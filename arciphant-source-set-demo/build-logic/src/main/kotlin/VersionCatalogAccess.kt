import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * Version catalog accessors for precompiled script plugins, where the type-safe
 * `libs` accessor is not available (https://github.com/gradle/gradle/issues/15383).
 */
internal fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary(alias).orElseThrow {
        IllegalArgumentException("No library with alias '$alias' in version catalog 'libs'.")
    }
