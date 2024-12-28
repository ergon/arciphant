package ch.cbossi.gradle.modulith

open class ModulithExtension {

    private val allModulesComponents = AllModulesConfigurationBuilder()
    private val modules = mutableSetOf<ModuleConfigurationBuilder>()
    private val bundles = mutableSetOf<BundleModuleConfigurationBuilder>()

    /**
     * See [basePlugin].
     */
    private var allModulesPlugin: Plugin = Plugin("kotlin")

    fun createComponent(name: String): ComponentReference = ComponentReference(name)

    fun allModules(configure: AllModulesConfigurationBuilder.() -> Unit = {}) {
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

    fun library(name: String, configure: ModuleConfigurationBuilder.() -> Unit = {}): LibraryModuleReference {
        return module(LibraryModuleReference(name), configure, isLibrary = true)
    }

    fun module(name: String, configure: ModuleConfigurationBuilder.() -> Unit = {}): DomainModuleReference {
        return module(DomainModuleReference(name), configure, isLibrary = false)
    }

    private fun <M : ComponentBasedModuleReference> module(
        reference: M,
        configure: ModuleConfigurationBuilder.() -> Unit = {},
        isLibrary: Boolean
    ): M {
        val module = ModuleConfigurationBuilder(reference, isLibrary)
        module.configure()
        modules.add(module)
        return reference
    }

    fun bundle(name: String? = null): BundleModuleConfigurationBuilder {
        val bundle = BundleModuleConfigurationBuilder(name?.emptyToNull())
        this@ModulithExtension.bundles.add(bundle)
        return bundle
    }

    internal fun getConfiguration() = ModulithConfiguration(
        componentBasedModules = modules.map { it.getConfiguration(allModulesComponents) },
        bundles = bundles.map { it.getConfiguration() },
    )

    private fun BundleModuleConfigurationBuilder.getConfiguration(): BundleModule {
        return BundleModule(
            reference = if(name != null) ChildBundleModuleReference(name) else RootBundleModuleReference,
            plugin = allModulesPlugin,
            modules = if (includedModules.isNotEmpty()) includedModules else modules.map { it.reference }
        )
    }

    private fun ModuleConfigurationBuilder.getConfiguration(allModulesConfiguration: AllModulesConfigurationBuilder): ComponentBasedModule {
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        val mergedComponents = mergedComponents(allModulesConfiguration).map {
            Component(
                reference = it,
                plugin = mergedComponentPlugins[it] ?: allModulesPlugin,
                dependsOn = dependencies.getValue(it)
            )
        }
        return if(isLibrary) {
            LibraryModule(reference, mergedComponents)
        } else {
            DomainModule(reference, mergedComponents)
        }
    }

    private fun ModuleConfigurationBuilder.mergedComponents(allModulesConfiguration: AllModulesConfigurationBuilder) =
        componentsInheritedFromAllModules(allModulesConfiguration) + components

    private fun ModuleConfigurationBuilder.componentsInheritedFromAllModules(
        allModulesConfiguration: AllModulesConfigurationBuilder
    ): List<ComponentReference> {
        return if (removeAllModulesComponents)
            emptyList()
        else
            allModulesConfiguration.components.filter { !removedAllModulesComponents.contains(it) }
    }

    private fun ModuleConfigurationBuilder.getDependencies(allModulesConfiguration: AllModulesConfigurationBuilder) =
        (allModulesConfiguration.dependencies + dependencies)
            .filter { !it.includesAny(removedAllModulesComponents) }
            .groupBy(ComponentDependency::source) { Dependency(it.dependsOn, it.type) }
            .withDefault { emptyList() }

    private fun ComponentDependency.includesAny(components: Collection<ComponentReference>) =
        components.any { source == it || dependsOn == it }
}
