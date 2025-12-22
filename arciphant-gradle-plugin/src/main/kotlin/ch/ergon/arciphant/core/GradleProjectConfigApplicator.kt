package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.LibraryModule
import ch.ergon.arciphant.core.model.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal class GradleProjectConfigApplicator(private val projectConfigs: List<GradleProjectConfig>) {

    private val projectConfigsByPath = projectConfigs.associateBy { it.path.value }

    private val libraryComponents = projectConfigs.filterIsInstance<GradleComponentProjectConfig>()
        .filter { it.module is LibraryModule }

    internal fun applyConfig(project: Project) {
        projectConfigsByPath[project.path]?.let {
            when (it) {
                is GradleBundleModuleProjectConfig -> it.applyBundleModuleConfig(project)
                is GradleComponentProjectConfig -> it.applyComponentConfig(project)
            }
        }
    }

    private fun GradleBundleModuleProjectConfig.applyBundleModuleConfig(bundleModuleProject: Project) {
        module.plugin?.applyTo(bundleModuleProject)

        projectConfigs.filter { module.includes.contains(it.module.reference) }.forEach {
            bundleModuleProject.addDependency(IMPLEMENTATION, it.path)
        }
    }

    private fun GradleComponentProjectConfig.applyComponentConfig(componentProject: Project) {
        component.plugin?.applyTo(componentProject)

        component.dependsOn.forEach {
            val dependencyProjectPath = module.gradleProjectPath(it.component)
            componentProject.addDependency(it.type, dependencyProjectPath)
        }

        if (module is DomainModule) {
            libraryComponents.filter { it.component.reference == component.reference }.forEach { library ->
                val dependencyProjectPath = library.module.gradleProjectPath(library.component)
                componentProject.addDependency(API, dependencyProjectPath)
            }
        }

        componentProject.tasks.withType(Jar::class.java).configureEach {
            this.archiveBaseName.set(module.createQualifiedComponentName(component))
        }
    }

    private fun Plugin.applyTo(project: Project) = project.apply(plugin = id)

}

private fun Project.addDependency(type: DependencyType, path: GradleProjectPath) {
    logger.info("Add ${type.configurationName} dependency: $path -> ${path.value}")
    addMainDependency(type, path)
    addTestFixturesDependency(path)
}

private fun Project.addMainDependency(type: DependencyType, path: GradleProjectPath) {
    try {
        dependencies { add(type.configurationName, project(path.value)) }
    } catch (e: UnknownConfigurationException) {
        throw IllegalArgumentException(
            """
            Arciphant error: configuration '${type.configurationName}' does not exist in project '${path.value}'.
            In order to use arciphant, you need to apply either 'java' or 'kotlin.jvm' plugin to all projects in order to get the required configurations ('implementation' and 'api').
            This is typically done either in the allprojects-block or inside a convention plugin registered in the arciphant configuration (see documentation).
            """.trimIndent(),
            e,
        )
    }
}

private fun Project.addTestFixturesDependency(path: GradleProjectPath) {
    pluginManager.withPlugin("java-test-fixtures") {
        dependencies { add("testFixturesApi", testFixtures(project(path.value))) }
    }
}
