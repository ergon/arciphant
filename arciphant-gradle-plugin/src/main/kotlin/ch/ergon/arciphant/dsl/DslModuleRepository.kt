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

    override fun load() = dsl.modules.map { it.createModule(dsl.allModules) } + dsl.bundles.map { it.createBundle() }

    private fun BundleModuleDsl.createBundle(): BundleModule {
        return BundleModule(
            reference = if (name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = plugin ?: dsl.allComponents.plugin,
            includes = includes.ifEmpty { dsl.modules.map { it.reference } }
        )
    }

    private fun SingleFunctionalModuleDsl.createModule(allModulesConfiguration: AllFunctionalModulesDsl): FunctionalModule {
        val stencil = build()
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + stencil.componentPlugins
        val dependencies = (allModulesConfiguration.dependencies + stencil.dependencies)
            .filter { !it.includesAny(removedAllModulesComponents) }
            .toDependencyMap()
        val mergedComponents = (componentsInheritedFromAllModules(allModulesConfiguration) + stencil.components).map {
            Component(
                reference = it,
                plugin = mergedComponentPlugins[it] ?: stencil.defaultComponentPlugin ?: dsl.allComponents.plugin,
                dependsOn = dependencies.getValue(it)
            )
        }
        return when (reference) {
            is DomainModuleReference -> DomainModule(reference, mergedComponents)
            is LibraryModuleReference -> LibraryModule(reference, mergedComponents)
        }
    }

    private fun SingleFunctionalModuleDsl.componentsInheritedFromAllModules(
        allModulesConfiguration: AllFunctionalModulesDsl
    ): List<ComponentReference> {
        return if (removeAllModulesComponents)
            emptyList()
        else
            allModulesConfiguration.components.filter { !removedAllModulesComponents.contains(it) }
    }

    private fun ComponentDependency.includesAny(components: Collection<ComponentReference>) =
        components.any { source == it || dependsOn == it }

    private fun List<ComponentDependency>.toDependencyMap() =
        groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

}