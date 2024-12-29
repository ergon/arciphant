package ch.cbossi.gradle.modulith

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ModulithSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.getByType(ModulithExtension::class.java)
            val structure = extension.createModuleStructure()
            structure.toGradleProjectStructure().forEach { include(it.value) }
            gradle.projectsLoaded {
                structure.createComposers(gradle.rootProject).forEach { it.configure() }
            }
        }

    }
}

private fun ModuleStructure.toGradleProjectStructure() = modules.flatMap { it.toGradleProjectStructure() }

private fun Module.toGradleProjectStructure() = when (this) {
    is ComponentBasedModule -> componentPaths()
    is BundleModule -> when (reference) {
        is ChildBundleModuleReference -> listOf(GradleProjectPath(reference.name))
        is RootBundleModuleReference -> listOf(GradleProjectPath())
    }
}

private fun ModuleStructure.createComposers(rootProject: Project) = modules.map {
    val project = it.reference.project(rootProject)
    when(it) {
        is LibraryModule -> LibraryModuleComposer(this, it, project)
        is DomainModule -> DomainModuleComposer(this, it, project)
        is BundleModule -> BundleModuleComposer(this, it, project)
    }
}

private fun ModuleReference.project(rootProject: Project) = when (this) {
    is ComponentBasedModuleReference -> rootProject.childProject(this)
    is ChildBundleModuleReference -> rootProject.childProject(this)
    is RootBundleModuleReference -> rootProject
}