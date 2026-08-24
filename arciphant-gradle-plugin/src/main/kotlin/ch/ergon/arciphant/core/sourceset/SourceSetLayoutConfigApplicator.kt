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
) {

    private val sourceSetComponentSettings = settings.sourceSetComponentSettings

    private val projectConfigsByPath = projectConfigs.associateBy { it.path.value }

    private val libraryModules = projectConfigs.filterIsInstance<GradleFunctionalModuleProjectConfig>()
        .filter { it.module is LibraryModule }

    fun applyConfig(project: Project) {
        val config = projectConfigsByPath[project.path] ?: return

        // This runs in lifecycle.beforeProject, i.e. before any other configuration of the project —
        // including the JVM plugin application that creates the source set container and the 'test'
        // and 'classes' tasks (typically done in the root project's allprojects block). Defer until
        // the java plugin (applied directly or through kotlin.jvm / java-library) is available.
        project.pluginManager.withPlugin(JAVA_PLUGIN_ID) {
            when (config) {
                is GradleBundleModuleProjectConfig -> config.applyBundleModuleConfig(project)
                is GradleFunctionalModuleProjectConfig -> config.applyFunctionalModuleConfig(project)
                is GradleComponentProjectConfig -> arcError(config.path)
            }
        }

        project.afterEvaluate {
            if (!project.pluginManager.hasPlugin(JAVA_PLUGIN_ID)) project.noJvmPluginError()
        }
    }

    private fun GradleBundleModuleProjectConfig.applyBundleModuleConfig(bundleModuleProject: Project) {
        projectConfigs.filter { module.includes.contains(it.module.reference) }.forEach {
            when(it) {
                is GradleFunctionalModuleProjectConfig -> {
                    it.module.components.forEach { component ->
                        bundleModuleProject.addSourceSetComponentDependency(it.path, component.reference.name)
                    }
                }
                is GradleBundleModuleProjectConfig -> {
                    bundleModuleProject.addMainDependency(
                        type = IMPLEMENTATION,
                        path = it.path,
                    )
                }
                is GradleComponentProjectConfig -> {
                    arcError(it.path)
                }
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

    private fun arcError(path: GradleProjectPath) {
        throw IllegalStateException("Arciphant error: unexpected component project '${path.value}' in component layout ${SOURCE_SET}.")
    }
}

private const val JAVA_PLUGIN_ID = "java"

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
