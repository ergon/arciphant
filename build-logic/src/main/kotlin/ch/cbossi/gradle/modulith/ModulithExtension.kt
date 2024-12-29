package ch.cbossi.gradle.modulith

open class ModulithExtension {

    private val allModulesComponents = AllComponentBasedModulesBuilder()
    private val modules = mutableSetOf<SingleComponentBasedModuleBuilder>()
    private val bundles = mutableSetOf<BundleModuleBuilder>()

    fun createComponent(name: String): ComponentReference = ComponentReference(name)

    fun allModules(configure: AllComponentBasedModulesBuilder.() -> Unit = {}) {
        allModulesComponents.configure()
    }

    fun allComponents(configure: AllComponentsBuilder.() -> Unit = {}) {
        AllComponentsBuilder.configure()
    }

    fun library(name: String, configure: SingleComponentBasedModuleBuilder.() -> Unit = {}): LibraryModuleReference {
        return module(LibraryModuleReference(name), configure)
    }

    fun module(name: String, configure: SingleComponentBasedModuleBuilder.() -> Unit = {}): DomainModuleReference {
        return module(DomainModuleReference(name), configure)
    }

    private fun <M : ComponentBasedModuleReference> module(reference: M, configure: SingleComponentBasedModuleBuilder.() -> Unit = {}): M {
        val module = SingleComponentBasedModuleBuilder(reference)
        module.configure()
        modules.add(module)
        return reference
    }

    fun bundle(name: String? = null): BundleModuleBuilder {
        val bundle = BundleModuleBuilder(name?.emptyToNull())
        this.bundles.add(bundle)
        return bundle
    }

    internal fun createModuleStructure() = ModuleStructure(
        modules = modules.map { it.createModule(allModulesComponents) } + bundles.map { it.createBundle() },
    )

    private fun BundleModuleBuilder.createBundle(): BundleModule {
        return BundleModule(
            reference = if (name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = AllComponentsBuilder.allComponentsPlugin,
            includes = if (includes.isNotEmpty()) includes else modules.map { it.reference }
        )
    }

    private fun SingleComponentBasedModuleBuilder.createModule(allModulesConfiguration: AllComponentBasedModulesBuilder): ComponentBasedModule {
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        val mergedComponents = mergeComponents(allModulesConfiguration).map {
            Component(
                reference = it,
                plugin = mergedComponentPlugins[it] ?: AllComponentsBuilder.allComponentsPlugin,
                dependsOn = dependencies.getValue(it)
            )
        }
        return when (reference) {
            is DomainModuleReference -> DomainModule(reference, mergedComponents)
            is LibraryModuleReference -> LibraryModule(reference, mergedComponents)
        }
    }

    private fun SingleComponentBasedModuleBuilder.mergeComponents(allModulesConfiguration: AllComponentBasedModulesBuilder) =
        componentsInheritedFromAllModules(allModulesConfiguration) + components

    private fun SingleComponentBasedModuleBuilder.componentsInheritedFromAllModules(
        allModulesConfiguration: AllComponentBasedModulesBuilder
    ): List<ComponentReference> {
        return if (removeAllModulesComponents)
            emptyList()
        else
            allModulesConfiguration.components.filter { !removedAllModulesComponents.contains(it) }
    }

    private fun SingleComponentBasedModuleBuilder.getDependencies(allModulesConfiguration: AllComponentBasedModulesBuilder) =
        (allModulesConfiguration.dependencies + dependencies)
            .filter { !it.includesAny(removedAllModulesComponents) }
            .groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

    private fun ComponentDependency.includesAny(components: Collection<ComponentReference>) =
        components.any { source == it || dependsOn == it }
}
