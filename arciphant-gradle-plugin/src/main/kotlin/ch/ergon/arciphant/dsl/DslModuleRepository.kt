package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*

internal class DslModuleRepository(private val dsl: ArciphantDsl) : ModuleRepository {

    override fun load() = dsl.modules.map { it.createFunctionalModule() } + dsl.bundles.map { it.createBundleModule() }

    private fun FunctionalModuleDsl.createFunctionalModule(): FunctionalModule {
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

    private fun BundleModuleDsl.createBundleModule(): BundleModule {
        return BundleModule(
            reference = if (name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = plugin,
            includes = includes.ifEmpty { dsl.modules.map { it.reference } }
        )
    }

}
