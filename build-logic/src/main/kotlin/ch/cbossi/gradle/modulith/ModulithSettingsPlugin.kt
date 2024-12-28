package ch.cbossi.gradle.modulith

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ModulithSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.getByType(ModulithExtension::class.java)
            val structure = extension.createModuleStructure()
            structure.toGradleProjectStructure().forEach { include(it) }
            gradle.projectsLoaded {
                structure.createModuleConfigurer(gradle.rootProject).forEach { it.configure() }
            }
        }

    }
}

private fun ModuleStructure.toGradleProjectStructure() = modules.flatMap { it.toGradleProjectStructure() }

private fun Module.toGradleProjectStructure() = when (this) {
    is ComponentBasedModule -> componentPaths()
    is BundleModule -> when (reference) {
        is ChildBundleModuleReference -> listOf(reference.name)
        is RootBundleModuleReference -> emptyList()
    }
}

private fun ModuleStructure.createModuleConfigurer(rootProject: Project) = modules.map {
    val project = it.reference.project(rootProject)
    when(it) {
        is ComponentBasedModule -> ComponentBasedModuleConfigurer(this, it, project)
        is BundleModule -> BundleModuleConfigurer(this, it, project)
    }
}

private fun ModuleReference.project(rootProject: Project) = when (this) {
    is ComponentBasedModuleReference -> rootProject.childProject(this.name)
    is ChildBundleModuleReference -> rootProject.childProject(this.name)
    is RootBundleModuleReference -> rootProject
}