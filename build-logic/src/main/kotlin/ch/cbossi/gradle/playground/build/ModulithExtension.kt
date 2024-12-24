package ch.cbossi.gradle.playground.build

open class ModulithExtension {

    private val defaultComponents = AllModulesConfigurationBuilder()
    private val modules = mutableSetOf<ModuleConfigurationBuilder>()

    fun createComponent(name: String): ComponentReference = ComponentReference(name)

    fun allModules(configure: AllModulesConfigurationBuilder.() -> Unit = {}) {
        defaultComponents.configure()
    }

    fun module(name: String, configure: ModuleConfigurationBuilder.() -> Unit = {}) {
        val module = ModuleConfigurationBuilder(name)
        module.configure()
        modules.add(module)
    }

    internal fun getConfiguration() = ModulithConfiguration(
        modules = modules.map { it.getConfiguration(defaultComponents) }
    )

    private fun ModuleConfigurationBuilder.getConfiguration(allModulesConfiguration: AllModulesConfigurationBuilder): Module {
        val allComponents = componentsInheritedFromAllModules(allModulesConfiguration) + components
        val allPlugins = allModulesConfiguration.componentPlugins + componentPlugins
        val dependencies = getDependencies(allModulesConfiguration)
        return Module(
            name = name,
            components = allComponents.map {
                Component(
                    reference = it,
                    plugin = allPlugins[it],
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
            .groupBy(ComponentDependency::source) { it.dependsOn }
            .withDefault { emptyList() }

    private fun ComponentDependency.includesAny(components: Collection<ComponentReference>) =
        components.any { source == it || dependsOn == it }
}
