package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.Plugin

data class Stencil internal constructor(
    internal val components: List<ComponentReference>,
    internal val dependencies: List<ComponentDependency>,
    internal val componentPlugins: Map<ComponentReference, Plugin>,
    internal val defaultComponentPlugin: Plugin?,
)
