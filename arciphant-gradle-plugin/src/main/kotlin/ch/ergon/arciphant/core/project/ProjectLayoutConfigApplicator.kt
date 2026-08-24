package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.ComponentLayout
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ConfigApplicator
import ch.ergon.arciphant.core.CoreSettings
import ch.ergon.arciphant.core.GradleBundleModuleProjectConfig
import ch.ergon.arciphant.core.GradleComponentProjectConfig
import ch.ergon.arciphant.core.GradleFunctionalModuleProjectConfig
import ch.ergon.arciphant.core.GradleProjectConfig
import ch.ergon.arciphant.core.addDependency
import ch.ergon.arciphant.core.applyTo
import ch.ergon.arciphant.core.createQualifiedComponentName
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.LibraryModule
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

internal class ProjectLayoutConfigApplicator(
    settings: CoreSettings,
    private val projectConfigs: List<GradleProjectConfig>
) : ConfigApplicator {

    private val projectComponentSettings = settings.projectComponentSettings

    private val projectConfigsByPath = projectConfigs.associateBy { it.path.value }

    private val libraryComponents = projectConfigs.filterIsInstance<GradleComponentProjectConfig>()
        .filter { it.module is LibraryModule }

    override fun applyConfig(project: Project) {
        projectConfigsByPath[project.path]?.let {
            when (it) {
                is GradleBundleModuleProjectConfig -> it.applyBundleModuleConfig(project)
                is GradleComponentProjectConfig -> it.applyComponentConfig(project)
                is GradleFunctionalModuleProjectConfig -> throw IllegalStateException(
                    "Arciphant error: unexpected functional module project '${project.path}' in component layout '${PROJECT}'."
                )
            }
        }
    }

    private fun GradleBundleModuleProjectConfig.applyBundleModuleConfig(bundleModuleProject: Project) {
        module.plugin?.applyTo(bundleModuleProject)

        projectConfigs.filter { module.includes.contains(it.module.reference) }.forEach {
            bundleModuleProject.addDependency(type = IMPLEMENTATION, path = it.path)
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

        configureArchiveBaseName(componentProject)
    }

    private fun GradleComponentProjectConfig.configureArchiveBaseName(componentProject: Project) {
        if(!projectComponentSettings.disableQualifiedArchiveBaseName) {
            componentProject.tasks.withType(Jar::class.java).configureEach {
                this.archiveBaseName.set(module.createQualifiedComponentName(component))
            }
        }
    }

}
