package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.*
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.LibraryModule
import ch.ergon.arciphant.core.model.Plugin
import ch.ergon.arciphant.util.arciphantError
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply

internal class ProjectLayoutConfigurer(
    settings: GlobalSettings,
    private val projectConfigs: List<GradleProjectConfig>
) : LayoutConfigurer(projectConfigs) {

    private val projectComponentSettings = settings.projectComponentSettings

    private val libraryComponents = projectConfigs.filterIsInstance<GradleComponentProjectConfig>()
        .filter { it.module is LibraryModule }

    override fun componentDependencyFactory(project: Project) = project.projectLayoutComponentDependency()

    override fun doConfigure(project: Project, config: GradleProjectConfig) {
        when (config) {
            is GradleBundleModuleProjectConfig -> config.applyBundleModuleConfig(project)
            is GradleComponentProjectConfig -> config.applyComponentConfig(project)
            is GradleFunctionalModuleProjectConfig -> arciphantError(
                "unexpected functional module project '${config.path.value}' in component layout '$PROJECT'."
            )
        }
    }

    private fun GradleBundleModuleProjectConfig.applyBundleModuleConfig(bundleModuleProject: Project) {
        module.plugin?.applyTo(bundleModuleProject)

        projectConfigs.filter { module.includes.contains(it.module.reference) }.forEach {
            bundleModuleProject.addDependency(type = IMPLEMENTATION, path = it.path)
        }
    }

    private fun GradleComponentProjectConfig.applyComponentConfig(componentProject: Project) {
        // completes component dependencies declared with the 'component' notation in dependencies blocks
        ProjectLayoutComponentDependencyCompleter(componentProject, componentProject.componentDependencyRegistry())
            .register()

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

        configureArchiveBaseName(componentProject)
    }

    private fun Plugin.applyTo(project: Project) = project.apply(plugin = id)

    private fun Project.addDependency(type: DependencyType, path: GradleProjectPath) {
        addMainDependency(type, path)
        addTestFixturesDependency(type, path)
    }

    private fun GradleComponentProjectConfig.configureArchiveBaseName(componentProject: Project) {
        if (!projectComponentSettings.disableQualifiedArchiveBaseName) {
            componentProject.tasks.withType(Jar::class.java).configureEach {
                this.archiveBaseName.set(module.createQualifiedComponentName(component))
            }
        }
    }

}
