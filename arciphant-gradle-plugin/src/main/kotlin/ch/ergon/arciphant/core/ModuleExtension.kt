package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.FunctionalModule

internal fun FunctionalModule.createQualifiedComponentName(component: Component): String {
    val moduleName = reference.name
    val componentName = component.reference.name
    val separator = if(moduleName.isNotEmpty() && componentName.isNotEmpty()) "-" else ""
    return moduleName + separator + componentName
}
