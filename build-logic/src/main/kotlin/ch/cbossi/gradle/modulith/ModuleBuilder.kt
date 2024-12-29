package ch.cbossi.gradle.modulith

sealed class ComponentBasedModuleBuilder {
    internal val components = mutableListOf<ComponentReference>()
    internal val dependencies = mutableListOf<ComponentDependency>()
    internal val componentPlugins = mutableMapOf<ComponentReference, Plugin>()

    fun addComponent(name: String) = addComponent(ComponentReference(name))

    fun addComponent(component: ComponentReference): ComponentReference {
        components.add(component)
        return component
    }

    /**
     * Overrides the base plugin for this component.
     * Important: Since the base plugin is overridden, the plugin itself should apply the base plugin
     */
    fun ComponentReference.withPlugin(id: String): ComponentReference {
        componentPlugins[this] = Plugin(id)
        return this
    }

    fun ComponentReference.dependsOnApi(vararg components: ComponentReference): ComponentReference {
        return dependsOn(components, DependencyType.API)
    }

    fun ComponentReference.dependsOn(vararg components: ComponentReference): ComponentReference {
        return dependsOn(components, DependencyType.IMPLEMENTATION)
    }

    private fun ComponentReference.dependsOn(
        components: Array<out ComponentReference>,
        type: DependencyType
    ): ComponentReference {
        dependencies.addAll(components.map { ComponentDependency(this, type, it) })
        return this
    }
}

class AllComponentsBuilder internal constructor() {

    internal var plugin: Plugin = Plugin("kotlin")
        private set

    /**
     * This plugin is applied to all components that do NOT specify a specific plugin.
     * The plugin configured here should transitively apply either the Java or Kotlin plugin.
     * The reason is that the modulith plugin requires the gradle configurations created by these JVM plugins
     * ('implementation', 'api') to apply the configured dependencies.
     * If no plugin is specified, the 'kotlin'-Plugin is applied as fallback.
     */
    fun plugin(id: String) {
        plugin = Plugin(id)
    }

}

class AllComponentBasedModulesBuilder internal constructor() : ComponentBasedModuleBuilder()

class SingleComponentBasedModuleBuilder internal constructor(
    internal val reference: ComponentBasedModuleReference,
) : ComponentBasedModuleBuilder() {
    internal var removeAllModulesComponents: Boolean = false
    internal val removedAllModulesComponents = mutableListOf<ComponentReference>()

    fun removeAllModulesComponents() {
        removeAllModulesComponents = true
    }

    fun removeComponent(vararg component: ComponentReference) {
        removedAllModulesComponents.addAll(component)
    }
}

internal data class ComponentDependency(
    val source: ComponentReference,
    val type: DependencyType,
    val dependsOn: ComponentReference,
)

class BundleModuleBuilder internal constructor(internal val name: String?) {

    internal val includes = mutableListOf<ComponentBasedModuleReference>()

    fun include(vararg modules: ComponentBasedModuleReference) {
        includes.addAll(modules)
    }

}
