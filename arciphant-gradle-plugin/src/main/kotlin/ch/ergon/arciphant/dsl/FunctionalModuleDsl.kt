package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.DependencyType
import ch.ergon.arciphant.model.FunctionalModuleReference
import ch.ergon.arciphant.model.Plugin
import ch.ergon.arciphant.util.merge

sealed class FunctionalModuleDsl {
    private var baseStencils = mutableListOf<Stencil>()
    private var defaultComponentPlugin: Plugin? = null

    internal val components = mutableListOf<ComponentReference>()
    internal val dependencies = mutableListOf<ComponentDependency>()
    internal val componentPlugins = mutableMapOf<ComponentReference, Plugin>()

    fun basedOn(vararg stencils: Stencil) {
        this.baseStencils.addAll(stencils)
    }

    fun defaultComponentPlugin(pluginId: String) {
        defaultComponentPlugin = Plugin(pluginId)
    }

    fun getComponent(componentName: String): ComponentReference {
        return ComponentReference(componentName)
    }

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

    fun ComponentReference.dependsOnApi(vararg componentNames: String) =
        dependsOnApi(componentNames.map { ComponentReference(it) })

    fun ComponentReference.dependsOnApi(vararg components: ComponentReference) = dependsOnApi(components.toList())

    private fun ComponentReference.dependsOnApi(components: Collection<ComponentReference>) =
        dependsOn(components, DependencyType.API)

    fun ComponentReference.dependsOn(vararg componentNames: String) =
        dependsOn(componentNames.map { ComponentReference(it) })

    fun ComponentReference.dependsOn(vararg components: ComponentReference) = dependsOn(components.toList())

    private fun ComponentReference.dependsOn(components: Collection<ComponentReference>) =
        dependsOn(components, DependencyType.IMPLEMENTATION)

    private fun ComponentReference.dependsOn(
        components: Collection<ComponentReference>,
        type: DependencyType
    ): ComponentReference {
        dependencies.addAll(components.map { ComponentDependency(this, type, it) })
        return this
    }

    internal fun build() = Stencil(
        components = baseStencils.flatMap { it.components } + components,
        dependencies = baseStencils.flatMap { it.dependencies } + dependencies,
        componentPlugins = baseStencils.map { it.componentPlugins }.merge() + componentPlugins,
        defaultComponentPlugin = defaultComponentPlugin ?: baseStencils.map { it.defaultComponentPlugin }.lastOrNull(),
    )
}

class StencilDsl internal constructor() : FunctionalModuleDsl()

class SingleFunctionalModuleDsl internal constructor(
    internal val reference: FunctionalModuleReference,
) : FunctionalModuleDsl()
