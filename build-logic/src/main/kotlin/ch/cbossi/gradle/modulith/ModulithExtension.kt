package ch.cbossi.gradle.modulith

open class ModulithExtension {

    private val allModulesComponents = AllComponentBasedModulesBuilder()
    private val modules = mutableSetOf<SingleComponentBasedModuleBuilder>()
    private val bundles = mutableSetOf<BundleModuleBuilder>()

    /**
     * See [basePlugin].
     */
    private var allModulesPlugin: Plugin = Plugin("kotlin")

    fun createComponent(name: String): ComponentReference = ComponentReference(name)

    fun allModules(configure: AllComponentBasedModulesBuilder.() -> Unit = {}) {
        allModulesComponents.configure()
    }

    /**
     * The base plugin is applied to all components that do NOT specify a specific plugin.
     * If you configure a base plugin, you should ensure that this plugin applies either the Java or Kotlin plugin.
     * The reason is that the modulith plugin requires the gradle configurations created by these JVM plugins
     * ('implementation', 'api') to apply the configured dependencies.
     * If no base plugin is specified, the 'kotlin'-Plugin is applied as fallback.
     */
    fun basePlugin(id: String) {
        allModulesPlugin = Plugin(id)
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
            plugin = allModulesPlugin,
            includes = if (includedModules.isNotEmpty()) includedModules else modules.map { it.reference }
        )
    }

    private fun SingleComponentBasedModuleBuilder.createModule(allModulesConfiguration: AllComponentBasedModulesBuilder): ComponentBasedModule {
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        val mergedComponents = mergeComponents(allModulesConfiguration).map {
            Component(
                reference = it,
                plugin = mergedComponentPlugins[it] ?: allModulesPlugin,
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
