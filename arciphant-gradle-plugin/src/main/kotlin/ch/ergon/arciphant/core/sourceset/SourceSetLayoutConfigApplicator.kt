package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.*
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.LibraryModule
import org.gradle.api.Project

internal class SourceSetLayoutConfigApplicator(
    settings: CoreSettings,
    private val projectConfigs: List<GradleProjectConfig>
) : ConfigApplicator {

    private val sourceSetComponentSettings = settings.sourceSetComponentSettings

    private val projectConfigsByPath = projectConfigs.associateBy { it.path.value }

    private val libraryModules = projectConfigs.filterIsInstance<GradleFunctionalModuleProjectConfig>()
        .filter { it.module is LibraryModule }

    override fun applyConfig(project: Project) {
        projectConfigsByPath[project.path]?.let {
            when (it) {
                is GradleBundleModuleProjectConfig -> it.applyBundleModuleConfig(project)
                is GradleFunctionalModuleProjectConfig -> it.applyFunctionalModuleConfig(project)
                is GradleComponentProjectConfig -> throw IllegalStateException(
                    "Arciphant error: unexpected component project '${project.path}' in component layout ${SOURCE_SET}."
                )
            }
        }
    }

    private fun GradleBundleModuleProjectConfig.applyBundleModuleConfig(bundleModuleProject: Project) {
        module.plugin?.applyTo(bundleModuleProject)

        projectConfigs.filter { module.includes.contains(it.module.reference) }.forEach {
            if (it is GradleFunctionalModuleProjectConfig) {
                it.module.components.forEach { component ->
                    bundleModuleProject.addSourceSetComponentDependency(it.path, component.reference.name)
                }
            } else {
                bundleModuleProject.addDependency(
                    type = IMPLEMENTATION,
                    path = it.path,
                    withTestFixturesSourceSet = false,
                )
            }
        }
    }

    private fun GradleFunctionalModuleProjectConfig.applyFunctionalModuleConfig(moduleProject: Project) {
        val sourceSetsByComponent = module.components.associateWith { component ->
            SourceSetFactory(moduleProject).createComponent(
                name = component.reference.name,
                settings = sourceSetComponentSettings,
                withTestSourceSet = component.withTestSourceSet,
                withTestFixturesSourceSet = component.withTestFixturesSourceSet,
            )
        }

        moduleProject.sourceSetDependencies(sourceSetComponentSettings) {
            sourceSetsByComponent.forEach { (component, sourceSets) ->
                component.dependsOn.forEach { dependency ->
                    val target = sourceSetsByComponent.entries.singleOrNull {
                        it.key.reference == dependency.component
                    } ?: throw IllegalArgumentException(
                        "Arciphant configuration error: Component '${component.reference.name}' depends on unknown component '${dependency.component.name}' in module '${module.reference.name}'."
                    )
                    addLocalDependency(dependency.type, sourceSets.production, target.value.production)
                }

                if (module is DomainModule) {
                    libraryModules.forEach { library ->
                        library.module.components
                            .filter { it.reference == component.reference }
                            .forEach { libraryComponent ->
                                addProjectDependency(
                                    type = API,
                                    sourceSet = sourceSets.production,
                                    projectPath = library.path.value,
                                    componentName = libraryComponent.reference.name,
                                    withTestFixturesSourceSet = libraryComponent.withTestFixturesSourceSet,
                                )
                            }
                    }
                }
            }
        }
    }

}

private fun Project.addSourceSetComponentDependency(path: GradleProjectPath, componentName: String) {
    dependencies.add(
        IMPLEMENTATION.configurationName,
        projectDependency(path.value, componentName.apiElementsConfigurationName()),
    )
    dependencies.add(
        "runtimeOnly",
        projectDependency(path.value, componentName.runtimeElementsConfigurationName()),
    )
}
