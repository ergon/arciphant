package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.*
import ch.ergon.arciphant.core.DependencyType
import ch.ergon.arciphant.core.Plugin

sealed class ArciphantDsl {

    protected val allModules = AllComponentBasedModulesDsl()
    protected val allComponents = AllComponentsDsl()
    protected val modules = mutableSetOf<SingleComponentBasedModuleDsl>()
    protected val bundles = mutableSetOf<BundleModuleDsl>()

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

}

class AllComponentsDsl internal constructor() {

    internal var plugin: Plugin = Plugin("kotlin")
        private set

    /**
     * This plugin is applied to all components that do NOT specify a specific plugin.
     * The plugin configured here should transitively apply either the Java or Kotlin plugin.
     * The reason is that the arciphant plugin requires the gradle configurations created by these JVM plugins
     * ('implementation', 'api') to apply the configured dependencies.
     * If no plugin is specified, the 'kotlin'-Plugin is applied as fallback.
     */
    fun plugin(id: String) {
        plugin = Plugin(id)
    }

}

sealed class ComponentBasedModuleDsl {
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

class AllComponentBasedModulesDsl internal constructor() : ComponentBasedModuleDsl()

class SingleComponentBasedModuleDsl internal constructor(
    internal val reference: ComponentBasedModuleReference,
) : ComponentBasedModuleDsl() {
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

class BundleModuleDsl internal constructor(internal val name: String?) {

    internal val includes = mutableListOf<ModuleReference>()

    fun include(vararg modules: ModuleReference) {
        includes.addAll(modules)
    }

}
