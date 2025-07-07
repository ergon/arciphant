package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*

internal class DslModuleRepository(private val dsl: ArciphantDsl) : ModuleRepository {

    override fun load() = dsl.modules.map { it.createFunctionalModule() } + dsl.bundles.map { it.createBundleModule() }

    private fun FunctionalModuleInstanceDsl.createFunctionalModule(): FunctionalModule {
        return with(build()) {
            require(components.isNotEmpty()) {
                "Module '${reference.name}' does not have any component. " +
                        "Functional modules (domain modules and libraries) cannot be created without components. " +
                        "Use bundle module instead if you need a module without components."
            }
            val dependenciesByComponent = dependencies.toDependencyMap()
            val components = components.map {
                Component(
                    reference = it,
                    plugin = componentPlugins[it] ?: defaultComponentPlugin,
                    dependsOn = dependenciesByComponent.getValue(it)
                )
            }
            when (reference) {
                is DomainModuleReference -> DomainModule(reference, components.toSet())
                is LibraryModuleReference -> LibraryModule(reference, components.toSet())
            }
        }
    }

    private fun List<ComponentDependency>.toDependencyMap() =
        groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

    private fun BundleModuleDsl.createBundleModule(): BundleModule {
        return BundleModule(
            reference = reference,
            plugin = plugin,
            includes = includes.ifEmpty { dsl.modules.map { it.reference } }.toSet()
        )
    }

}
