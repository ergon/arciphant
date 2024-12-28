package ch.cbossi.gradle.modulith

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ModulithSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.getByType(ModulithExtension::class.java)
            val configuration = extension.createModuleStructure()
            configuration.createProjectStructure().forEach { include(it) }
            gradle.projectsLoaded {
                configuration.configureModules(gradle.rootProject)
            }
        }

    }
}

private fun ModuleStructure.createProjectStructure() = modules.flatMap { it.createProjectStructure() }

private fun Module.createProjectStructure() = when (this) {
    is ComponentBasedModule -> componentPaths()
    is BundleModule -> when (reference) {
        is ChildBundleModuleReference -> listOf(reference.name)
        is RootBundleModuleReference -> emptyList()
    }
}

private fun ModuleStructure.configureModules(rootProject: Project) {
    componentBasedModules.forEach { ComponentBasedModuleConfigurer(this, it, rootProject.childProject(it.name)).configure() }
    bundles.forEach { BundleModuleConfigurer(this, it, it.project(rootProject)).configure() }
}

private fun BundleModule.project(rootProject: Project) = when (this.reference) {
    is RootBundleModuleReference -> rootProject
    is ChildBundleModuleReference -> rootProject.childProject(this.reference.name)
}