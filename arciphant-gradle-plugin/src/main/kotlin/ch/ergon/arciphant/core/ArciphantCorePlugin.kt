package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.ArciphantDsl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ArciphantCorePlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.getByType(ArciphantDsl::class.java)
            val structure = ModuleStructureRepositoryImpl(extension).create()
            structure.toGradleProjectPaths().forEach { include(it.value) }
            gradle.projectsLoaded {
                structure.createComposers(gradle.rootProject).forEach { it.configure() }
            }
        }

    }
}

private fun ModuleStructure.toGradleProjectPaths() = modules.flatMap { it.gradleProjectPaths() }.filter { !it.isRoot }

private fun ModuleStructure.createComposers(rootProject: Project) = modules.map {
    val project = it.reference.project(rootProject)
    when (it) {
        is LibraryModule -> LibraryModuleComposer(this, it, project)
        is DomainModule -> DomainModuleComposer(this, it, project)
        is BundleModule -> BundleModuleComposer(this, it, project)
    }
}

private fun ModuleReference.project(rootProject: Project) = when (this) {
    is FunctionalModuleReference -> rootProject.childProject(this)
    is ChildBundleModuleReference -> rootProject.childProject(this)
    is RootBundleModuleReference -> rootProject
}
