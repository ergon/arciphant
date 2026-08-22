package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.sourceset.SourceSetFactory
import ch.ergon.arciphant.core.sourceset.apiElementsConfigurationName
import ch.ergon.arciphant.core.sourceset.projectDependency
import ch.ergon.arciphant.core.sourceset.runtimeElementsConfigurationName
import ch.ergon.arciphant.core.sourceset.sourceSetDependencies
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal class GradleProjectConfigApplicator(
    private val settings: CoreSettings,
    private val projectConfigs: List<GradleProjectConfig>
) {

    private val projectComponentSettings = settings.projectComponentSettings
    private val sourceSetComponentSettings = settings.sourceSetComponentSettings

    private val projectConfigsByPath = projectConfigs.associateBy { it.path.value }

    private val libraryComponents = projectConfigs.filterIsInstance<GradleComponentProjectConfig>()
        .filter { it.module is LibraryModule }

    private val libraryModules = projectConfigs.filterIsInstance<GradleFunctionalModuleProjectConfig>()
        .filter { it.module is LibraryModule }

    internal fun applyConfig(project: Project) {
        projectConfigsByPath[project.path]?.let {
            when (it) {
                is GradleBundleModuleProjectConfig -> it.applyBundleModuleConfig(project)
                is GradleComponentProjectConfig -> it.applyComponentConfig(project)
                is GradleFunctionalModuleProjectConfig -> it.applyFunctionalModuleConfig(project)
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
                    withTestFixturesSourceSet = settings.componentLayout == PROJECT,
                )
            }
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

    private fun Plugin.applyTo(project: Project) = project.apply(plugin = id)

    private fun GradleComponentProjectConfig.configureArchiveBaseName(componentProject: Project) {
        if(!settings.projectComponentSettings.disableQualifiedArchiveBaseName) {
            componentProject.tasks.withType(Jar::class.java).configureEach {
                this.archiveBaseName.set(module.createQualifiedComponentName(component))
            }
        }
    }

}

private fun Project.addDependency(
    type: DependencyType,
    path: GradleProjectPath,
    withTestFixturesSourceSet: Boolean = true,
) {
    logger.info("Add ${type.configurationName} dependency: $path -> ${path.value}")
    addMainDependency(type, path)
    if (withTestFixturesSourceSet) addTestFixturesDependency(path)
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
