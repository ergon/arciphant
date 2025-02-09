package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.BundleModule
import ch.ergon.arciphant.model.ChildBundleModuleReference
import ch.ergon.arciphant.model.Component
import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.Dependency
import ch.ergon.arciphant.model.DomainModule
import ch.ergon.arciphant.model.DomainModuleReference
import ch.ergon.arciphant.model.FunctionalModule
import ch.ergon.arciphant.model.LibraryModule
import ch.ergon.arciphant.model.LibraryModuleReference
import ch.ergon.arciphant.model.ModuleRepository
import ch.ergon.arciphant.model.RootBundleModuleReference

internal class DslModuleRepository(private val dsl: ArciphantDsl) : ModuleRepository {

    override fun load() = dsl.modules.map { it.createModule() } + dsl.bundles.map { it.createBundle() }

    private fun BundleModuleDsl.createBundle(): BundleModule {
        return BundleModule(
            reference = if (name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = plugin,
            includes = includes.ifEmpty { dsl.modules.map { it.reference } }
        )
    }

    private fun SingleFunctionalModuleDsl.createModule(): FunctionalModule {
        val stencil = build()
        val dependencies = stencil.dependencies.toDependencyMap()
        val mergedComponents = stencil.components.map {
            Component(
                reference = it,
                plugin = stencil.componentPlugins[it] ?: stencil.defaultComponentPlugin,
                dependsOn = dependencies.getValue(it)
            )
        }
        return when (reference) {
            is DomainModuleReference -> DomainModule(reference, mergedComponents)
            is LibraryModuleReference -> LibraryModule(reference, mergedComponents)
        }
    }

    private fun List<ComponentDependency>.toDependencyMap() =
        groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

}