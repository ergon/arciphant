package ch.ergon.arciphant.core

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
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

    private fun GradleBundleModuleProjectConfig.applyBundleModuleConfig(project: Project) {
        module.plugin?.applyTo(project)

        projectConfigs.filter { module.includes.contains(it.module.reference) }.forEach {
            project.addDependency(IMPLEMENTATION, it.path)
        }
    }

    private fun GradleComponentProjectConfig.applyComponentConfig(project: Project) {
        component.plugin?.applyTo(project)

        component.dependsOn.forEach {
            val dependencyProjectPath = module.gradleProjectPath(it.component)
            project.addDependency(it.type, dependencyProjectPath)
        }

        if(module is DomainModule) {
            libraryComponents.filter { it.component.reference == component.reference }.forEach { library ->
                val dependencyProjectPath = library.module.gradleProjectPath(library.component)
                project.addDependency(API, dependencyProjectPath)
            }
        }
    }

    private fun Plugin.applyTo(project: Project) = project.apply(plugin = id)

}

private fun Project.addDependency(type: DependencyType, path: GradleProjectPath) {
    logger.info("Add ${type.configurationName} dependency: $path -> ${path.value}")
    dependencies { add(type.configurationName, project(path.value)) }
    pluginManager.withPlugin("java-test-fixtures") {
        dependencies { add("testFixturesApi", testFixtures(project(path.value))) }
    }
}
