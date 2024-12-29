package ch.cbossi.gradle.modulith

open class ModulithExtension : ModulithDsl() {

    internal fun createModuleStructure() = ModuleStructure(
        modules = modules.map { it.createModule(allModules) } + bundles.map { it.createBundle() },
    )

    private fun BundleModuleDsl.createBundle(): BundleModule {
        return BundleModule(
            reference = if (name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = allComponents.plugin,
            includes = if (includes.isNotEmpty()) includes else modules.map { it.reference }
        )
    }

    private fun SingleComponentBasedModuleDsl.createModule(allModulesConfiguration: AllComponentBasedModulesDsl): ComponentBasedModule {
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        val mergedComponents = mergeComponents(allModulesConfiguration).map {
            Component(
                reference = it,
                plugin = mergedComponentPlugins[it] ?: allComponents.plugin,
                dependsOn = dependencies.getValue(it)
            )
        }
        return when (reference) {
            is DomainModuleReference -> DomainModule(reference, mergedComponents)
            is LibraryModuleReference -> LibraryModule(reference, mergedComponents)
        }
    }

    private fun SingleComponentBasedModuleDsl.mergeComponents(allModulesConfiguration: AllComponentBasedModulesDsl) =
        componentsInheritedFromAllModules(allModulesConfiguration) + components

    private fun SingleComponentBasedModuleDsl.componentsInheritedFromAllModules(
        allModulesConfiguration: AllComponentBasedModulesDsl
    ): List<ComponentReference> {
        return if (removeAllModulesComponents)
            emptyList()
        else
            allModulesConfiguration.components.filter { !removedAllModulesComponents.contains(it) }
    }

    private fun SingleComponentBasedModuleDsl.getDependencies(allModulesConfiguration: AllComponentBasedModulesDsl) =
        (allModulesConfiguration.dependencies + dependencies)
            .filter { !it.includesAny(removedAllModulesComponents) }
            .groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

    private fun ComponentDependency.includesAny(components: Collection<ComponentReference>) =
        components.any { source == it || dependsOn == it }
}
