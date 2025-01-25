package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.DependencyType
import ch.ergon.arciphant.model.FunctionalModuleReference
import ch.ergon.arciphant.model.Plugin

sealed class FunctionalModuleDsl {
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
    fun ComponentReference.withPlugin(pluginId: String): ComponentReference {
        componentPlugins[this] = Plugin(pluginId)
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

internal data class ComponentDependency(
    val source: ComponentReference,
    val type: DependencyType,
    val dependsOn: ComponentReference,
)

class AllFunctionalModulesDsl internal constructor() : FunctionalModuleDsl()

class SingleFunctionalModuleDsl internal constructor(
    internal val reference: FunctionalModuleReference,
) : FunctionalModuleDsl() {
    internal var removeAllModulesComponents: Boolean = false
    internal val removedAllModulesComponents = mutableListOf<ComponentReference>()

    fun removeAllModulesComponents() {
        removeAllModulesComponents = true
    }

    fun removeComponent(vararg components: ComponentReference) {
        removedAllModulesComponents.addAll(components)
    }
}
