package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.*
import ch.ergon.arciphant.model.*

internal class ModuleRepositoryImpl(private val dsl: ArciphantDsl) : ModuleRepository {

    override fun load() = dsl.modules.map { it.createModule(dsl.allModules) } + dsl.bundles.map { it.createBundle() }

    private fun BundleModuleDsl.createBundle(): BundleModule {
        return BundleModule(
            reference = if (name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = plugin ?: dsl.allComponents.plugin,
            includes = includes.ifEmpty { dsl.modules.map { it.reference } }
        )
    }

    private fun SingleFunctionalModuleDsl.createModule(allModulesConfiguration: AllFunctionalModulesDsl): FunctionalModule {
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        val mergedComponents = mergeComponents(allModulesConfiguration).map {
            Component(
                reference = it,
                plugin = mergedComponentPlugins[it] ?: dsl.allComponents.plugin,
                dependsOn = dependencies.getValue(it)
            )
        }
        return when (reference) {
            is DomainModuleReference -> DomainModule(reference, mergedComponents)
            is LibraryModuleReference -> LibraryModule(reference, mergedComponents)
        }
    }

    private fun SingleFunctionalModuleDsl.mergeComponents(allModulesConfiguration: AllFunctionalModulesDsl) =
        componentsInheritedFromAllModules(allModulesConfiguration) + components

    private fun SingleFunctionalModuleDsl.componentsInheritedFromAllModules(
        allModulesConfiguration: AllFunctionalModulesDsl
    ): List<ComponentReference> {
        return if (removeAllModulesComponents)
            emptyList()
        else
            allModulesConfiguration.components.filter { !removedAllModulesComponents.contains(it) }
    }

    private fun SingleFunctionalModuleDsl.getDependencies(allModulesConfiguration: AllFunctionalModulesDsl) =
        (allModulesConfiguration.dependencies + dependencies)
            .filter { !it.includesAny(removedAllModulesComponents) }
            .groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

    private fun ComponentDependency.includesAny(components: Collection<ComponentReference>) =
        components.any { source == it || dependsOn == it }

}
