package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.Plugin
import ch.ergon.arciphant.util.merge

class StencilDsl : FunctionalModuleDsl() {

    private var baseStencils = mutableListOf<Stencil>()
    private var defaultComponentPlugin: Plugin? = null

    fun basedOn(vararg stencils: Stencil) {
        this.baseStencils.addAll(stencils)
    }

    fun defaultComponentPlugin(pluginId: String) {
        defaultComponentPlugin = Plugin(pluginId)
    }

    internal fun build() = Stencil(
        components = baseStencils.flatMap { it.components } + components,
        dependencies = baseStencils.flatMap { it.dependencies } + dependencies,
        componentPlugins = baseStencils.map { it.componentPlugins }.merge() + componentPlugins,
        defaultComponentPlugin = defaultComponentPlugin ?: baseStencils.map { it.defaultComponentPlugin }.last(),
    )
}

data class Stencil internal constructor(
    internal val components: List<ComponentReference>,
    internal val dependencies: List<ComponentDependency>,
    internal val componentPlugins: Map<ComponentReference, Plugin>,
    internal val defaultComponentPlugin: Plugin?,
)
