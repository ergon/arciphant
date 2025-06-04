package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.dsl.DslModuleRepository
import ch.ergon.arciphant.model.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class ArciphantCorePlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.create("arciphant", ArciphantDsl::class.java)

            gradle.settingsEvaluated {
                val modules = DslModuleRepository(extension).load()
                modules.toGradleProjectPaths().forEach { include(it.value) }

                gradle.beforeProject {
                    modules.createComposers(gradle.rootProject).forEach { it.configure() }
                }
            }
        }

    }
}

private fun List<Module>.toGradleProjectPaths() = flatMap { it.gradleProjectPaths() }.filter { !it.isRoot }

private fun List<Module>.createComposers(rootProject: Project) = map {
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
