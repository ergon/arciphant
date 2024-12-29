package ch.cbossi.gradle.modulith

open class ModulithExtension {

    private val allModules = AllComponentBasedModulesDsl()
    private val allComponents = AllComponentsDsl()
    private val modules = mutableSetOf<SingleComponentBasedModuleDsl>()
    private val bundles = mutableSetOf<BundleModuleDsl>()

    fun createComponent(name: String): ComponentReference = ComponentReference(name)

    fun allModules(configure: AllComponentBasedModulesDsl.() -> Unit = {}) {
        allModules.configure()
    }

    fun allComponents(configure: AllComponentsDsl.() -> Unit = {}) {
        allComponents.configure()
    }

    fun library(name: String, configure: SingleComponentBasedModuleDsl.() -> Unit = {}): LibraryModuleReference {
        return module(LibraryModuleReference(name), configure)
    }

    fun module(name: String, configure: SingleComponentBasedModuleDsl.() -> Unit = {}): DomainModuleReference {
        return module(DomainModuleReference(name), configure)
    }

    private fun <M : ComponentBasedModuleReference> module(reference: M, configure: SingleComponentBasedModuleDsl.() -> Unit = {}): M {
        val module = SingleComponentBasedModuleDsl(reference)
        module.configure()
        modules.add(module)
        return reference
    }

    fun bundle(name: String? = null): BundleModuleDsl {
        val bundle = BundleModuleDsl(name?.emptyToNull())
        this.bundles.add(bundle)
        return bundle
    }

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
