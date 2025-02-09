package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.DependencyType

internal data class ComponentDependency(
    val source: ComponentReference,
    val type: DependencyType,
    val dependsOn: ComponentReference,
)