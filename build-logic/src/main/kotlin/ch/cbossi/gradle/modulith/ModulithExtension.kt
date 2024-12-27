package ch.cbossi.gradle.modulith

open class ModulithExtension {

    private val allModulesComponents = AllModulesConfigurationBuilder()
    private val modules = mutableSetOf<ModuleConfigurationBuilder>()

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

    fun library(name: String, configure: ModuleConfigurationBuilder.() -> Unit = {}) {
        module(name, configure, isLibrary = true)
    }

    fun module(name: String, configure: ModuleConfigurationBuilder.() -> Unit = {}) {
        module(name, configure, isLibrary = false)
    }

    private fun module(name: String, configure: ModuleConfigurationBuilder.() -> Unit = {}, isLibrary: Boolean) {
        val module = ModuleConfigurationBuilder(name, isLibrary)
        module.configure()
        modules.add(module)
    }

    internal fun getConfiguration() = ModulithConfiguration(
        modules = modules.map { it.getConfiguration(allModulesComponents) },
    )

    private fun ModuleConfigurationBuilder.getConfiguration(allModulesConfiguration: AllModulesConfigurationBuilder): Module {
        val mergedComponents = componentsInheritedFromAllModules(allModulesConfiguration) + components
        val mergedComponentPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        return Module(
            name = name,
            isLibrary = isLibrary,
            components = mergedComponents.map {
                Component(
                    reference = it,
                    plugin = mergedComponentPlugins[it] ?: allModulesPlugin,
                    dependsOn = dependencies.getValue(it)
                )
            },
        )
    }

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
